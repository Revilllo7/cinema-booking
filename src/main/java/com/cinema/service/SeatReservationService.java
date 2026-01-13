package com.cinema.service;

import com.cinema.dto.SeatMapResponse;
import com.cinema.dto.SeatStatusDTO;
import com.cinema.dto.SeatStatusDTO.SeatState;
import com.cinema.entity.BookingSeat;
import com.cinema.entity.BookingSeat.SeatStatus;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.SeatLock;
import com.cinema.entity.SeatLock.SeatLockStatus;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingSeatRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatStatusNotifierService seatStatusNotifier;

    @Value("${app.seating.lock-duration-minutes:10}")
    private int lockDurationMinutes;

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long screeningId, String sessionId, String username) {
        Screening screening = getScreening(screeningId);
        List<SeatStatusDTO> seats = buildSeatStatuses(screening, sessionId, username);
        return SeatMapResponse.builder()
            .rows(screening.getHall().getRowsCount())
            .cols(screening.getHall().getSeatsPerRow())
            .holdMinutes(lockDurationMinutes)
            .seats(seats)
            .build();
    }

    @Transactional
    public SeatStatusDTO lockSeat(Long screeningId, Long seatId, String sessionId, String username) {
        Screening screening = getScreening(screeningId);
        Seat seat = getSeatForScreening(seatId, screening);

        LocalDateTime now = LocalDateTime.now();
        assertSeatIsAvailable(screeningId, seatId);

        Optional<SeatLock> existingLock = seatLockRepository.findActiveLock(screeningId, seatId, now);
        if (existingLock.isPresent()) {
            SeatLock lock = existingLock.get();
            if (isSameRequester(lock, sessionId, username)) {
                lock.setExpiresAt(now.plusMinutes(lockDurationMinutes));
                log.debug("Extending lock {} for seat {}", lock.getId(), seatId);
                return buildSeatStatus(seat, Map.of(), Map.of(seat.getId(), lock), sessionId, username);
            }
            throw new IllegalStateException("Seat already locked by another user");
        }

        SeatLock lock = SeatLock.builder()
            .seat(seat)
            .screening(screening)
            .sessionId(sessionId)
            .username(username)
            .expiresAt(now.plusMinutes(lockDurationMinutes))
            .status(SeatLockStatus.ACTIVE)
            .build();
        seatLockRepository.save(lock);
        log.info("Seat {} locked for screening {} by session {}", seatId, screeningId, sessionId);

        broadcast(screeningId);
        return buildSeatStatus(seat, Map.of(), Map.of(seat.getId(), lock), sessionId, username);
    }

    @Transactional
    public void releaseSeat(Long screeningId, Long seatId, String sessionId, String username) {
        Optional<SeatLock> lockOptional = findLockForOwner(screeningId, seatId, sessionId, username);
        if (lockOptional.isEmpty()) {
            log.debug("No active lock to release for seat {}", seatId);
            return;
        }
        SeatLock lock = lockOptional.get();

        if (!isSameRequester(lock, sessionId, username)) {
            throw new IllegalStateException("Cannot release another user's seat lock");
        }

        lock.setStatus(SeatLockStatus.RELEASED);
        log.info("Seat {} released for screening {} by session {}", seatId, screeningId, sessionId);
        broadcast(screeningId);
    }

    @Transactional
    public void releaseAll(Long screeningId, String sessionId) {
        List<SeatLock> locks = seatLockRepository.findActiveLocksByScreening(screeningId, LocalDateTime.now()).stream()
            .filter(lock -> lock.getSessionId().equals(sessionId))
            .toList();
        if (locks.isEmpty()) {
            return;
        }
        locks.forEach(lock -> lock.setStatus(SeatLockStatus.RELEASED));
        log.info("Released {} locks for session {}", locks.size(), sessionId);
        broadcast(screeningId);
    }

    @Transactional
    public void expireLocks() {
        List<SeatLock> expired = seatLockRepository.findByStatusAndExpiresAtBefore(SeatLockStatus.ACTIVE, LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }

        Map<Long, List<SeatLock>> locksByScreening = expired.stream()
            .collect(Collectors.groupingBy(lock -> lock.getScreening().getId()));

        locksByScreening.forEach((screeningId, locks) -> {
            locks.forEach(lock -> lock.setStatus(SeatLockStatus.EXPIRED));
            log.info("Expired {} seat locks for screening {}", locks.size(), screeningId);
            broadcast(screeningId);
        });
    }

    @Transactional(readOnly = true)
    public List<SeatStatusDTO> buildSeatStatuses(Long screeningId) {
        Screening screening = getScreening(screeningId);
        return buildSeatStatuses(screening, null, null);
    }

    @Transactional(readOnly = true)
    public void broadcastSeatMap(Long screeningId) {
        broadcast(screeningId);
    }

    private void broadcast(Long screeningId) {
        List<SeatStatusDTO> payload = buildSeatStatuses(screeningId);
        seatStatusNotifier.broadcast(screeningId, payload);
    }

    private Screening getScreening(Long screeningId) {
        return screeningRepository.findById(screeningId)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));
    }

    private Seat getSeatForScreening(Long seatId, Screening screening) {
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));
        if (!seat.getHall().getId().equals(screening.getHall().getId())) {
            throw new IllegalArgumentException("Seat does not belong to screening hall");
        }
        return seat;
    }

    private void assertSeatIsAvailable(Long screeningId, Long seatId) {
        boolean booked = bookingSeatRepository.findBySeatIdAndScreeningId(seatId, screeningId).stream()
            .anyMatch(bs -> bs.getSeatStatus() != SeatStatus.AVAILABLE);
        if (booked) {
            throw new IllegalStateException("Seat is already reserved");
        }
    }

    private boolean isSameRequester(SeatLock lock, String sessionId, String username) {
        if (lock.getSessionId().equals(sessionId)) {
            return true;
        }
        return username != null && username.equals(lock.getUsername());
    }

    private List<SeatStatusDTO> buildSeatStatuses(Screening screening, String sessionId, String username) {
        Long screeningId = screening.getId();
        Map<Long, BookingSeat> bookedSeats = bookingSeatRepository.findActiveSeatsByScreeningId(screeningId).stream()
            .sorted(Comparator.comparing(bs -> bs.getSeat().getId()))
            .collect(Collectors.toMap(bs -> bs.getSeat().getId(), Function.identity(), (first, second) -> first));

        Map<Long, SeatLock> locks = seatLockRepository.findActiveLocksByScreening(screeningId, LocalDateTime.now()).stream()
            .collect(Collectors.toMap(lock -> lock.getSeat().getId(), Function.identity(),
                (first, second) -> first.getExpiresAt().isAfter(second.getExpiresAt()) ? first : second));

        return seatRepository.findByHallIdAndActiveTrue(screening.getHall().getId()).stream()
            .map(seat -> buildSeatStatus(seat, bookedSeats, locks, sessionId, username))
            .collect(Collectors.toList());
    }

    private SeatStatusDTO buildSeatStatus(Seat seat,
                                          Map<Long, BookingSeat> bookedSeats,
                                          Map<Long, SeatLock> locks,
                                          String sessionId,
                                          String username) {
        BookingSeat bookingSeat = bookedSeats.get(seat.getId());
        if (bookingSeat != null) {
            return SeatStatusDTO.builder()
                .seatId(seat.getId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .status(convertBookingStatus(bookingSeat.getSeatStatus()))
                .selectedByYou(false)
                .lockExpiresAt(null)
                .build();
        }

        SeatLock lock = locks.get(seat.getId());
        boolean selectedByYou = lock != null && isSameRequester(lock, sessionId, username);
        return SeatStatusDTO.builder()
            .seatId(seat.getId())
            .rowNumber(seat.getRowNumber())
            .seatNumber(seat.getSeatNumber())
            .status(lock != null ? SeatState.BOOKED : SeatState.FREE)
            .selectedByYou(selectedByYou)
            .lockExpiresAt(lock != null ? lock.getExpiresAt() : null)
            .build();
    }

    private SeatState convertBookingStatus(SeatStatus status) {
        return switch (status) {
            case OCCUPIED -> SeatState.SOLD;
            case RESERVED -> SeatState.BOOKED;
            default -> SeatState.BOOKED;
        };
    }

    private Optional<SeatLock> findLockForOwner(Long screeningId, Long seatId, String sessionId, String username) {
        Optional<SeatLock> lock = seatLockRepository.findActiveLockForSession(screeningId, seatId, sessionId, LocalDateTime.now());
        if (lock.isEmpty() && username != null && !username.isBlank()) {
            lock = seatLockRepository.findActiveLockForUsername(screeningId, seatId, username, LocalDateTime.now());
        }
        return lock;
    }
}
