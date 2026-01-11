package com.cinema.service;

import com.cinema.dto.ScreeningDTO;
import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.DTOFixtures;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Unit tests for ScreeningService using Mockito.
 * Tests screening management business logic in isolation from database.
 */
@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private ScreeningService screeningService;

    private Screening testScreening;
    private Movie testMovie;
    private Hall testHall;

    @BeforeEach
    void setUp() {
        testMovie = EntityFixtures.createDefaultMovie();
        testMovie.setId(1L);
        
        testHall = EntityFixtures.createDefaultHall();
        testHall.setId(1L);
        
        testScreening = EntityFixtures.createUpcomingScreening();
        testScreening.setId(1L);
        testScreening.setMovie(testMovie);
        testScreening.setHall(testHall);
    }

    // ========== getAllActiveScreenings Tests ==========

    @Test
    void getAllActiveScreenings_WithUpcomingScreenings_ReturnsPaginatedScreeningDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> screeningPage = new PageImpl<>(List.of(testScreening));
        given(screeningRepository.findUpcomingScreenings(any(LocalDateTime.class), any(Pageable.class)))
            .willReturn(screeningPage);

        // When
        Page<ScreeningDTO> result = screeningService.getAllActiveScreenings(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllActiveScreenings_CallsRepository_WithCurrentTime() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> screeningPage = new PageImpl<>(List.of(testScreening));
        given(screeningRepository.findUpcomingScreenings(any(LocalDateTime.class), any(Pageable.class)))
            .willReturn(screeningPage);

        // When
        screeningService.getAllActiveScreenings(pageable);

        // Then
        then(screeningRepository).should(times(1))
            .findUpcomingScreenings(any(LocalDateTime.class), any(Pageable.class));
    }

    // ========== getUpcomingScreenings Tests ==========

    @Test
    void getUpcomingScreenings_WithFromDate_ReturnsScreeningsAfterDate() {
        // Given
        LocalDateTime fromDate = LocalDateTime.of(2026, 1, 15, 10, 0);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> screeningPage = new PageImpl<>(List.of(testScreening));
        given(screeningRepository.findUpcomingScreenings(fromDate, pageable))
            .willReturn(screeningPage);

        // When
        Page<ScreeningDTO> result = screeningService.getUpcomingScreenings(fromDate, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getUpcomingScreenings_CallsRepository_WithCorrectParameters() {
        // Given
        LocalDateTime fromDate = LocalDateTime.of(2026, 1, 15, 10, 0);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> screeningPage = new PageImpl<>(List.of(testScreening));
        given(screeningRepository.findUpcomingScreenings(fromDate, pageable))
            .willReturn(screeningPage);

        // When
        screeningService.getUpcomingScreenings(fromDate, pageable);

        // Then
        then(screeningRepository).should(times(1))
            .findUpcomingScreenings(fromDate, pageable);
    }

    // ========== getScreeningById Tests ==========

    @Test
    void getScreeningById_ExistingScreening_ReturnsScreeningDTO() {
        // Given
        given(screeningRepository.findById(1L)).willReturn(Optional.of(testScreening));

        // When
        ScreeningDTO result = screeningService.getScreeningById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getScreeningById_ExistingScreening_MapsAllFields() {
        // Given
        given(screeningRepository.findById(1L)).willReturn(Optional.of(testScreening));

        // When
        ScreeningDTO result = screeningService.getScreeningById(1L);

        // Then
        assertThat(result.getMovieId()).isEqualTo(testMovie.getId());
        assertThat(result.getHallId()).isEqualTo(testHall.getId());
        assertThat(result.getActive()).isTrue();
    }

    @Test
    void getScreeningById_NonExistingScreening_ThrowsResourceNotFoundException() {
        // Given
        given(screeningRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> screeningService.getScreeningById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getScreeningsByMovie Tests ==========

    @Test
    void getScreeningsByMovie_ExistingMovie_ReturnsScreeningsForMovie() {
        // Given
        List<Screening> screenings = List.of(testScreening);
        given(screeningRepository.findByMovieIdAndStartTimeAfter(any(Long.class), any(LocalDateTime.class)))
            .willReturn(screenings);

        // When
        List<ScreeningDTO> result = screeningService.getScreeningsByMovie(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getScreeningsByMovie_NonExistingMovie_ReturnsEmptyList() {
        // Given
        given(screeningRepository.findByMovieIdAndStartTimeAfter(any(Long.class), any(LocalDateTime.class)))
            .willReturn(List.of());

        // When
        List<ScreeningDTO> result = screeningService.getScreeningsByMovie(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getScreeningsByMovie_CallsRepository_WithMovieIdAndCurrentTime() {
        // Given
        given(screeningRepository.findByMovieIdAndStartTimeAfter(any(Long.class), any(LocalDateTime.class)))
            .willReturn(List.of(testScreening));

        // When
        screeningService.getScreeningsByMovie(1L);

        // Then
        then(screeningRepository).should(times(1))
            .findByMovieIdAndStartTimeAfter(any(Long.class), any(LocalDateTime.class));
    }

    // ========== getScreeningsByHallAndDateRange Tests ==========

    @Test
    void getScreeningsByHallAndDateRange_WithValidRange_ReturnsScreeningsInRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 20, 22, 0);
        List<Screening> screenings = List.of(testScreening);
        given(screeningRepository.findByHallIdAndStartTimeBetween(1L, startDate, endDate))
            .willReturn(screenings);

        // When
        List<ScreeningDTO> result = screeningService.getScreeningsByHallAndDateRange(1L, startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getScreeningsByHallAndDateRange_EmptyRange_ReturnsEmptyList() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 15, 11, 0);
        given(screeningRepository.findByHallIdAndStartTimeBetween(1L, startDate, endDate))
            .willReturn(List.of());

        // When
        List<ScreeningDTO> result = screeningService.getScreeningsByHallAndDateRange(1L, startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== createScreening Tests ==========

    @Test
    void createScreening_ValidScreening_ReturnsCreatedScreeningDTO() {
        // Given
        ScreeningDTO newScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        Screening savedScreening = EntityFixtures.screeningBuilder()
            .id(1L)
            .movie(testMovie)
            .hall(testHall)
            .build();

        given(movieRepository.findById(newScreeningDTO.getMovieId()))
            .willReturn(Optional.of(testMovie));
        given(hallRepository.findById(newScreeningDTO.getHallId()))
            .willReturn(Optional.of(testHall));
        given(screeningRepository.save(any(Screening.class)))
            .willReturn(savedScreening);

        // When
        ScreeningDTO result = screeningService.createScreening(newScreeningDTO);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createScreening_ValidScreening_SetsActiveToTrue() {
        // Given
        ScreeningDTO newScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        Screening savedScreening = EntityFixtures.screeningBuilder()
            .id(1L)
            .active(true)
            .movie(testMovie)
            .hall(testHall)
            .build();

        given(movieRepository.findById(newScreeningDTO.getMovieId()))
            .willReturn(Optional.of(testMovie));
        given(hallRepository.findById(newScreeningDTO.getHallId()))
            .willReturn(Optional.of(testHall));
        given(screeningRepository.save(any(Screening.class)))
            .willReturn(savedScreening);

        // When
        ScreeningDTO result = screeningService.createScreening(newScreeningDTO);

        // Then
        assertThat(result.getActive()).isTrue();
    }

    @Test
    void createScreening_NonExistentMovie_ThrowsResourceNotFoundException() {
        // Given
        ScreeningDTO newScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        given(movieRepository.findById(newScreeningDTO.getMovieId()))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> screeningService.createScreening(newScreeningDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Movie");
    }

    @Test
    void createScreening_NonExistentHall_ThrowsResourceNotFoundException() {
        // Given
        ScreeningDTO newScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        given(movieRepository.findById(newScreeningDTO.getMovieId()))
            .willReturn(Optional.of(testMovie));
        given(hallRepository.findById(newScreeningDTO.getHallId()))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> screeningService.createScreening(newScreeningDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Hall");
    }

    @Test
    void createScreening_InvalidTimeRange_ThrowsIllegalArgumentException() {
        // Given
        ScreeningDTO invalidScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        invalidScreeningDTO.setStartTime(LocalDateTime.of(2026, 1, 20, 22, 0));
        invalidScreeningDTO.setEndTime(LocalDateTime.of(2026, 1, 20, 20, 0)); // End before start
        
        given(movieRepository.findById(invalidScreeningDTO.getMovieId()))
            .willReturn(Optional.of(testMovie));
        given(hallRepository.findById(invalidScreeningDTO.getHallId()))
            .willReturn(Optional.of(testHall));

        // When & Then
        assertThatThrownBy(() -> screeningService.createScreening(invalidScreeningDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void createScreening_ValidScreening_CallsRepositorySave() {
        // Given
        ScreeningDTO newScreeningDTO = DTOFixtures.createScreeningDTOWithoutId();
        Screening savedScreening = EntityFixtures.screeningBuilder()
            .id(1L)
            .movie(testMovie)
            .hall(testHall)
            .build();

        given(movieRepository.findById(newScreeningDTO.getMovieId()))
            .willReturn(Optional.of(testMovie));
        given(hallRepository.findById(newScreeningDTO.getHallId()))
            .willReturn(Optional.of(testHall));
        given(screeningRepository.save(any(Screening.class)))
            .willReturn(savedScreening);

        // When
        screeningService.createScreening(newScreeningDTO);

        // Then
        then(screeningRepository).should(times(1)).save(any(Screening.class));
    }

    // ========== updateScreening Tests ==========

    @Test
    void updateScreening_ExistingScreening_ReturnsUpdatedScreeningDTO() {
        // Given
        ScreeningDTO updateDTO = DTOFixtures.createDefaultScreeningDTO();
        given(screeningRepository.findById(1L)).willReturn(Optional.of(testScreening));
        given(screeningRepository.save(any(Screening.class)))
            .willReturn(testScreening);

        // When
        ScreeningDTO result = screeningService.updateScreening(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void updateScreening_NonExistingScreening_ThrowsResourceNotFoundException() {
        // Given
        ScreeningDTO updateDTO = DTOFixtures.createDefaultScreeningDTO();
        given(screeningRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> screeningService.updateScreening(999L, updateDTO))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateScreening_NonExistentMovieInUpdate_ThrowsResourceNotFoundException() {
        // Given
        ScreeningDTO updateDTO = DTOFixtures.createDefaultScreeningDTO();
        updateDTO.setMovieId(999L); // Different movie
        
        given(screeningRepository.findById(1L)).willReturn(Optional.of(testScreening));
        given(movieRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> screeningService.updateScreening(1L, updateDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Movie");
    }

    @Test
    void updateScreening_ExistingScreening_CallsRepositorySave() {
        // Given
        ScreeningDTO updateDTO = DTOFixtures.createDefaultScreeningDTO();
        given(screeningRepository.findById(1L)).willReturn(Optional.of(testScreening));
        given(screeningRepository.save(any(Screening.class)))
            .willReturn(testScreening);

        // When
        screeningService.updateScreening(1L, updateDTO);

        // Then
        then(screeningRepository).should(times(1)).save(any(Screening.class));
    }

    // ========== deleteScreening Tests ==========

    @Test
    void deleteScreening_ExistingScreening_CallsRepositoryDelete() {
        // Given
        given(screeningRepository.existsById(1L)).willReturn(true);

        // When
        screeningService.deleteScreening(1L);

        // Then
        then(screeningRepository).should(times(1)).deleteById(1L);
    }

    @Test
    void deleteScreening_NonExistingScreening_ThrowsResourceNotFoundException() {
        // Given
        given(screeningRepository.existsById(999L)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> screeningService.deleteScreening(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteScreening_NonExistingScreening_DoesNotCallRepositoryDelete() {
        // Given
        given(screeningRepository.existsById(999L)).willReturn(false);

        // When & Then
        try {
            screeningService.deleteScreening(999L);
        } catch (ResourceNotFoundException e) {
            // Expected
        }
        then(screeningRepository).should(never()).deleteById(any());
    }
}
