package com.cinema.service;

import com.cinema.dto.ScreeningDTO;
import com.cinema.entity.BookingSeat;
import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingSeatRepository;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAllActiveScreenings(Pageable pageable) {
        log.debug("Fetching all active screenings with pagination: {}", pageable);
        return screeningRepository.findUpcomingScreenings(LocalDateTime.now(), pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getUpcomingScreenings(LocalDateTime from, Pageable pageable) {
        log.debug("Fetching upcoming screenings from: {}", from);
        return screeningRepository.findUpcomingScreenings(from, pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public ScreeningDTO getScreeningById(Long id) {
        log.debug("Fetching screening by id: {}", id);
        Screening screening = screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
        return convertToDto(screening);
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByMovie(Long movieId) {
        log.debug("Fetching screenings for movie id: {}", movieId);
        return screeningRepository.findByMovieIdAndStartTimeAfter(movieId, LocalDateTime.now())
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByHall(Long hallId) {
        log.debug("Fetching screenings for hall id: {}", hallId);
        return screeningRepository.findByHallIdAndStartTimeAfter(hallId, LocalDateTime.now())
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByHallAndDateRange(Long hallId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching screenings for hall id: {}, between {} and {}", hallId, startDate, endDate);
        return screeningRepository.findByHallIdAndStartTimeBetween(hallId, startDate, endDate)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public ScreeningDTO createScreening(ScreeningDTO screeningDTO) {
        log.info("Creating new screening for movie id: {} in hall id: {}", 
            screeningDTO.getMovieId(), screeningDTO.getHallId());

        // Validate that movie exists
        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screeningDTO.getMovieId()));

        // Validate that hall exists
        Hall hall = hallRepository.findById(screeningDTO.getHallId())
            .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", screeningDTO.getHallId()));

        // Validate time range
        if (screeningDTO.getStartTime().isAfter(screeningDTO.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Screening screening = convertToEntity(screeningDTO);
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setActive(true);

        Screening savedScreening = screeningRepository.save(screening);
        log.info("Screening created successfully with id: {}", savedScreening.getId());

        return convertToDto(savedScreening);
    }

    @Transactional
    public ScreeningDTO updateScreening(Long id, ScreeningDTO screeningDTO) {
        log.info("Updating screening with id: {}", id);

        Screening existingScreening = screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));

        // Validate movie exists if changed
        if (!existingScreening.getMovie().getId().equals(screeningDTO.getMovieId())) {
            Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screeningDTO.getMovieId()));
            existingScreening.setMovie(movie);
        }

        // Validate hall exists if changed
        if (!existingScreening.getHall().getId().equals(screeningDTO.getHallId())) {
            Hall hall = hallRepository.findById(screeningDTO.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", screeningDTO.getHallId()));
            existingScreening.setHall(hall);
        }

        updateEntityFromDto(existingScreening, screeningDTO);

        Screening updatedScreening = screeningRepository.save(existingScreening);
        log.info("Screening updated successfully: {}", updatedScreening.getId());

        return convertToDto(updatedScreening);
    }

    @Transactional
    public void cancelScreening(Long id) {
        log.info("Cancelling screening with id: {}", id);
        Screening screening = screeningRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
        screening.setActive(false);
        screeningRepository.save(screening);
        log.info("Screening cancelled successfully: {}", id);
    }

    @Transactional
    public void deleteScreening(Long id) {
        log.info("Deleting screening with id: {}", id);
        if (!screeningRepository.existsById(id)) {
            throw new ResourceNotFoundException("Screening", "id", id);
        }
        screeningRepository.deleteById(id);
        log.info("Screening deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMapForScreening(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));

        Hall hall = screening.getHall();

        List<String> occupied = bookingSeatRepository.findActiveSeatsByScreeningId(screeningId).stream()
            .map(BookingSeat::getSeat)
            .map(seat -> seat.getRowNumber() + "-" + seat.getSeatNumber())
            .toList();

        return new SeatMapResponse(hall.getRowsCount(), hall.getSeatsPerRow(), occupied);
    }

    public record SeatMapResponse(int rows, int cols, List<String> occupied) {}

    // Mapping methods
    private ScreeningDTO convertToDto(Screening screening) {
        return ScreeningDTO.builder()
            .id(screening.getId())
            .movieId(screening.getMovie().getId())
            .movieTitle(screening.getMovie().getTitle())
            .hallId(screening.getHall().getId())
            .hallName(screening.getHall().getName())
            .startTime(screening.getStartTime())
            .endTime(screening.getEndTime())
            .basePrice(screening.getBasePrice())
            .active(screening.getActive())
            .availableSeats(screening.getHall().getTotalSeats() - 
                (screening.getBookings() != null ? 
                    screening.getBookings().stream()
                        .mapToInt(b -> b.getBookingSeats().size())
                        .sum() : 0))
            .createdAt(screening.getCreatedAt())
            .build();
    }

    private Screening convertToEntity(ScreeningDTO dto) {
        return Screening.builder()
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .basePrice(dto.getBasePrice())
            .active(dto.getActive() != null ? dto.getActive() : true)
            .build();
    }

    private void updateEntityFromDto(Screening screening, ScreeningDTO dto) {
        screening.setStartTime(dto.getStartTime());
        screening.setEndTime(dto.getEndTime());
        screening.setBasePrice(dto.getBasePrice());
        if (dto.getActive() != null) {
            screening.setActive(dto.getActive());
        }
    }
}
