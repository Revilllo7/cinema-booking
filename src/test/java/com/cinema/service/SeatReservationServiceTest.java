package com.cinema.service;

import com.cinema.dto.SeatStatusDTO;
import com.cinema.entity.Hall;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.SeatLock;
import com.cinema.entity.SeatLock.SeatLockStatus;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingSeatRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatReservationServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private SeatLockRepository seatLockRepository;
    @Mock
    private SeatStatusNotifierService seatStatusNotifier;

    @InjectMocks
    private SeatReservationService seatReservationService;

    private Screening screening;
    private Hall hall;
    private Seat seat;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seatReservationService, "lockDurationMinutes", 5);

        hall = Hall.builder()
            .id(3L)
            .rowsCount(10)
            .seatsPerRow(12)
            .build();
        screening = Screening.builder()
            .id(7L)
            .hall(hall)
            .build();
        seat = Seat.builder()
            .id(42L)
            .hall(hall)
            .rowNumber(4)
            .seatNumber(8)
            .build();
    }

    @Test
    void lockSeat_WhenAvailable_CreatesLockAndBroadcasts() {
        when(screeningRepository.findById(7L)).thenReturn(Optional.of(screening)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(42L)).thenReturn(Optional.of(seat));
        when(seatRepository.findByHallIdAndActiveTrue(hall.getId())).thenReturn(List.of(seat));
        when(bookingSeatRepository.findBySeatIdAndScreeningId(42L, 7L)).thenReturn(List.of());
        when(bookingSeatRepository.findActiveSeatsByScreeningId(7L)).thenReturn(List.of());
        when(seatLockRepository.findActiveLock(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(seatLockRepository.findActiveLocksByScreening(anyLong(), any(LocalDateTime.class))).thenReturn(List.of());
        when(seatLockRepository.save(any(SeatLock.class))).thenAnswer(invocation -> {
            SeatLock lock = invocation.getArgument(0);
            lock.setId(99L);
            return lock;
        });

        SeatStatusDTO status = seatReservationService.lockSeat(7L, 42L, "session-1", "jane");

        assertThat(status.getSeatId()).isEqualTo(42L);
        assertThat(status.isSelectedByYou()).isTrue();

        ArgumentCaptor<SeatLock> lockCaptor = ArgumentCaptor.forClass(SeatLock.class);
        verify(seatLockRepository).save(lockCaptor.capture());
        SeatLock persisted = lockCaptor.getValue();
        assertThat(persisted.getSeat()).isEqualTo(seat);
        assertThat(persisted.getScreening()).isEqualTo(screening);
        assertThat(persisted.getSessionId()).isEqualTo("session-1");
        assertThat(persisted.getStatus()).isEqualTo(SeatLockStatus.ACTIVE);

        verify(seatStatusNotifier).broadcast(anyLong(), anyList());
    }

    @Test
    void releaseSeat_WhenHeldByDifferentUser_Throws() {
        SeatLock lock = SeatLock.builder()
            .id(15L)
            .seat(seat)
            .screening(screening)
            .sessionId("other-session")
            .username("someone")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(3))
            .build();

        when(seatLockRepository.findActiveLockForSession(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
            .thenReturn(Optional.of(lock));

        assertThatThrownBy(() -> seatReservationService.releaseSeat(7L, 42L, "session-1", "jane"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot release another user's seat lock");

        assertThat(lock.getStatus()).isEqualTo(SeatLockStatus.ACTIVE);
        verify(seatLockRepository).findActiveLockForSession(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
        verify(seatStatusNotifier, never()).broadcast(anyLong(), any());
    }

    @Test
    void releaseAll_WhenSessionHasLocks_MarksReleasedAndBroadcasts() {
        SeatLock sessionLock = SeatLock.builder()
            .id(1L)
            .screening(screening)
            .seat(seat)
            .sessionId("session-1")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(2))
            .build();
        SeatLock otherLock = SeatLock.builder()
            .id(2L)
            .screening(screening)
            .seat(seat)
            .sessionId("session-2")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(2))
            .build();

        when(seatLockRepository.findActiveLocksByScreening(anyLong(), any(LocalDateTime.class)))
            .thenReturn(List.of(sessionLock, otherLock));
        when(screeningRepository.findById(7L)).thenReturn(Optional.of(screening));
        when(seatRepository.findByHallIdAndActiveTrue(hall.getId())).thenReturn(List.of(seat));
        when(bookingSeatRepository.findActiveSeatsByScreeningId(7L)).thenReturn(List.of());

        seatReservationService.releaseAll(7L, "session-1");

        assertThat(sessionLock.getStatus()).isEqualTo(SeatLockStatus.RELEASED);
        assertThat(otherLock.getStatus()).isEqualTo(SeatLockStatus.ACTIVE);
        verify(seatLockRepository, times(2)).findActiveLocksByScreening(anyLong(), any(LocalDateTime.class));
        verify(seatStatusNotifier).broadcast(anyLong(), anyList());
    }

    @Test
    void lockSeat_WhenScreeningNotFound_Throws() {
        when(screeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatReservationService.lockSeat(999L, 42L, "session-1", "jane"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Screening");

        verify(screeningRepository).findById(999L);
    }

    @Test
    void lockSeat_WhenSeatNotFound_Throws() {
        when(screeningRepository.findById(7L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatReservationService.lockSeat(7L, 999L, "session-1", "jane"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Seat");

        verify(seatRepository).findById(999L);
    }

    @Test
    void lockSeat_WhenSeatAlreadyLocked_Throws() {
        SeatLock existingLock = SeatLock.builder()
            .id(1L)
            .seat(seat)
            .screening(screening)
            .sessionId("other-session")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(3))
            .build();

        when(screeningRepository.findById(7L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(42L)).thenReturn(Optional.of(seat));
        when(bookingSeatRepository.findBySeatIdAndScreeningId(42L, 7L)).thenReturn(List.of());
        when(seatLockRepository.findActiveLock(eq(7L), eq(42L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(existingLock));

        assertThatThrownBy(() -> seatReservationService.lockSeat(7L, 42L, "session-1", "jane"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already locked");

        verify(seatLockRepository).findActiveLock(eq(7L), eq(42L), any(LocalDateTime.class));
    }

    @Test
    void releaseSeat_WhenNoLock_NoOp() {
        when(seatLockRepository.findActiveLockForSession(anyLong(), anyLong(), anyString(), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        assertThatCode(() -> seatReservationService.releaseSeat(7L, 42L, "session-1", "jane"))
            .doesNotThrowAnyException();

        verify(seatLockRepository).findActiveLockForSession(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
        verify(seatStatusNotifier, never()).broadcast(anyLong(), anyList());
    }

    @Test
    void releaseSeat_WhenHeldByYou_Releases() {
        SeatLock lock = SeatLock.builder()
            .id(15L)
            .seat(seat)
            .screening(screening)
            .sessionId("session-1")
            .username("jane")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(3))
            .build();

        when(seatLockRepository.findActiveLockForSession(eq(7L), eq(42L), eq("session-1"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(lock));
        when(screeningRepository.findById(screening.getId())).thenReturn(Optional.of(screening));
        when(seatRepository.findByHallIdAndActiveTrue(hall.getId())).thenReturn(List.of(seat));
        when(bookingSeatRepository.findActiveSeatsByScreeningId(screening.getId())).thenReturn(List.of());
        when(seatLockRepository.findActiveLocksByScreening(eq(screening.getId()), any(LocalDateTime.class)))
            .thenReturn(List.of());

        seatReservationService.releaseSeat(7L, 42L, "session-1", "jane");

        assertThat(lock.getStatus()).isEqualTo(SeatLockStatus.RELEASED);
        verify(seatLockRepository).findActiveLockForSession(eq(7L), eq(42L), eq("session-1"), any(LocalDateTime.class));
        verify(seatStatusNotifier).broadcast(anyLong(), anyList());
    }

    @Test
    void getSeatMap_WhenScreeningNotFound_Throws() {
        when(screeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatReservationService.getSeatMap(999L, "session-1", "jane"))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(screeningRepository).findById(999L);
    }

    @Test
    void releaseAll_WithNoLocks_CompletesWithoutError() {
        when(seatLockRepository.findActiveLocksByScreening(anyLong(), any(LocalDateTime.class)))
            .thenReturn(List.of());

        seatReservationService.releaseAll(7L, "session-1");

        verify(seatLockRepository).findActiveLocksByScreening(anyLong(), any(LocalDateTime.class));
        verify(seatStatusNotifier, never()).broadcast(anyLong(), anyList());
    }

    @Test
    void releaseAll_WithExpiredLocks_ReleasesExpiredOnly() {
        LocalDateTime now = LocalDateTime.now();
        SeatLock expiredLock = SeatLock.builder()
            .id(1L)
            .screening(screening)
            .seat(seat)
            .sessionId("session-1")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(now.minusMinutes(1))
            .build();
        SeatLock activeLock = SeatLock.builder()
            .id(2L)
            .screening(screening)
            .seat(seat)
            .sessionId("session-1")
            .status(SeatLockStatus.ACTIVE)
            .expiresAt(now.plusMinutes(5))
            .build();

        when(seatLockRepository.findActiveLocksByScreening(anyLong(), any(LocalDateTime.class)))
            .thenReturn(List.of(expiredLock, activeLock));
        when(screeningRepository.findById(7L)).thenReturn(Optional.of(screening));
        when(seatRepository.findByHallIdAndActiveTrue(hall.getId())).thenReturn(List.of(seat));
        when(bookingSeatRepository.findActiveSeatsByScreeningId(7L)).thenReturn(List.of());

        seatReservationService.releaseAll(7L, "session-1");

        verify(seatLockRepository, times(2)).findActiveLocksByScreening(anyLong(), any(LocalDateTime.class));
        verify(seatStatusNotifier).broadcast(anyLong(), anyList());
    }
}
