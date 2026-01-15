package com.cinema.service;

import com.cinema.dto.ScreeningDTO;
import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.exception.ResourceNotFoundException;
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

    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAllActiveScreenings(Pageable pageable) {
        log.debug("Fetching all active screenings with pagination: {}", pageable);
        return screeningRepository.findUpcomingScreenings(LocalDateTime.now(), pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAllActiveScreenings(Pageable pageable, Long movieId, String startDate, String endDate) {
        log.debug("Fetching screenings with pagination: {}, movieId: {}, startDate: {}, endDate: {}", pageable, movieId, startDate, endDate);
        
        LocalDateTime start = startDate != null && !startDate.isBlank() ? 
            LocalDateTime.parse(startDate.contains("T") ? startDate : startDate + "T00:00:00") : 
            LocalDateTime.now();
        LocalDateTime end = endDate != null && !endDate.isBlank() ? 
            LocalDateTime.parse(endDate.contains("T") ? endDate : endDate + "T23:59:59") : 
            LocalDateTime.now().plusDays(365);
        
        if (movieId != null && movieId > 0) {
            return screeningRepository.findByActiveTrueAndMovieIdAndStartTimeBetween(movieId, start, end, pageable)
                .map(this::convertToDto);
        } else {
            return screeningRepository.findByActiveTrueAndStartTimeBetween(start, end, pageable)
                .map(this::convertToDto);
        }
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

        LocalDateTime startTime = screeningDTO.getStartTime();
        LocalDateTime endTime = resolveEndTime(screeningDTO, movie);

        validateTimeRange(startTime, endTime);
        assertNoConflicts(hall.getId(), null, startTime, endTime);

        screeningDTO.setEndTime(endTime);

        Screening screening = convertToEntity(screeningDTO);
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
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

        Movie targetMovie = existingScreening.getMovie();
        if (!existingScreening.getMovie().getId().equals(screeningDTO.getMovieId())) {
            targetMovie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", screeningDTO.getMovieId()));
            existingScreening.setMovie(targetMovie);
        }

        Hall targetHall = existingScreening.getHall();
        if (!existingScreening.getHall().getId().equals(screeningDTO.getHallId())) {
            targetHall = hallRepository.findById(screeningDTO.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", screeningDTO.getHallId()));
            existingScreening.setHall(targetHall);
        }

        LocalDateTime startTime = screeningDTO.getStartTime();
        LocalDateTime endTime = resolveEndTime(screeningDTO, targetMovie);

        validateTimeRange(startTime, endTime);
        assertNoConflicts(targetHall.getId(), existingScreening.getId(), startTime, endTime);

        screeningDTO.setEndTime(endTime);
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

    // Mapping methods
    private ScreeningDTO convertToDto(Screening screening) {
        return ScreeningDTO.builder()
            .id(screening.getId())
            .movieId(screening.getMovie().getId())
            .movieTitle(screening.getMovie().getTitle())
            .moviePosterPath(screening.getMovie().getPosterPath())
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

    private LocalDateTime resolveEndTime(ScreeningDTO dto, Movie movie) {
        if (dto.getEndTime() != null) {
            return dto.getEndTime();
        }
        if (dto.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (movie.getDurationMinutes() == null) {
            throw new IllegalStateException("Movie duration is required to calculate end time");
        }
        return dto.getStartTime().plusMinutes(movie.getDurationMinutes());
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void assertNoConflicts(Long hallId, Long screeningId, LocalDateTime start, LocalDateTime end) {
        Long exclusionId = screeningId != null ? screeningId : -1L;
        boolean conflict = screeningRepository.existsConflictingScreening(hallId, exclusionId, start, end);
        if (conflict) {
            throw new IllegalStateException("Screening overlaps with another screening in the same hall");
        }
    }
}
