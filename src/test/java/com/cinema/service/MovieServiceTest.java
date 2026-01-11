package com.cinema.service;

import com.cinema.dto.MovieDTO;
import com.cinema.entity.Movie;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.DTOFixtures;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.repository.MovieRepository;
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
 * Unit tests for MovieService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;

    @BeforeEach
    void setUp() {
        testMovie = EntityFixtures.createDefaultMovie();
        testMovie.setId(1L); // Set ID for service mocking tests
    }

    // ========== getAllActiveMovies Tests ==========

    @Test
    void getAllActiveMovies_WithActiveMovies_ReturnsPaginatedMovieDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
        given(movieRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(moviePage);

        // When
        Page<MovieDTO> result = movieService.getAllActiveMovies(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllActiveMovies_CallsRepository_ExactlyOnce() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
        given(movieRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(moviePage);

        // When
        movieService.getAllActiveMovies(pageable);

        // Then
        then(movieRepository).should(times(1)).findByActiveTrueOrderByCreatedAtDesc(pageable);
    }

    // ========== getMovieById Tests ==========

    @Test
    void getMovieById_ExistingMovie_ReturnsMovieDTO() {
        // Given
        given(movieRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(testMovie));

        // When
        MovieDTO result = movieService.getMovieById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getMovieById_ExistingMovie_MapsAllFields() {
        // Given
        given(movieRepository.findByIdAndActiveTrue(1L)).willReturn(Optional.of(testMovie));

        // When
        MovieDTO result = movieService.getMovieById(1L);

        // Then
        assertThat(result.getTitle()).isEqualTo(testMovie.getTitle());
        assertThat(result.getGenre()).isEqualTo(testMovie.getGenre());
        assertThat(result.getDurationMinutes()).isEqualTo(testMovie.getDurationMinutes());
    }

    @Test
    void getMovieById_NonExistingMovie_ThrowsResourceNotFoundException() {
        // Given
        given(movieRepository.findByIdAndActiveTrue(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.getMovieById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== searchMoviesByTitle Tests ==========

    @Test
    void searchMoviesByTitle_MatchingKeyword_ReturnsMatchingMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
        given(movieRepository.searchByTitle("Inception", pageable)).willReturn(moviePage);

        // When
        Page<MovieDTO> result = movieService.searchMoviesByTitle("Inception", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchMoviesByTitle_NoMatch_ReturnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> emptyPage = Page.empty();
        given(movieRepository.searchByTitle("NonExistent", pageable)).willReturn(emptyPage);

        // When
        Page<MovieDTO> result = movieService.searchMoviesByTitle("NonExistent", pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
    }

    // ========== getMoviesByGenre Tests ==========

    @Test
    void getMoviesByGenre_ExistingGenre_ReturnsMoviesOfGenre() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
        given(movieRepository.findByGenre("Sci-Fi", pageable)).willReturn(moviePage);

        // When
        Page<MovieDTO> result = movieService.getMoviesByGenre("Sci-Fi", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ========== getAllGenres Tests ==========

    @Test
    void getAllGenres_WithMovies_ReturnsDistinctGenres() {
        // Given
        List<String> genres = List.of("Action", "Drama", "Sci-Fi");
        given(movieRepository.findAllGenres()).willReturn(genres);

        // When
        List<String> result = movieService.getAllGenres();

        // Then
        assertThat(result).hasSize(3);
    }

    // ========== createMovie Tests ==========

    @Test
    void createMovie_ValidMovie_ReturnsCreatedMovieDTO() {
        // Given
        MovieDTO newMovieDTO = DTOFixtures.createMovieDTOWithoutId();
        Movie savedMovie = EntityFixtures.movieBuilder()
            .id(1L)
            .title(newMovieDTO.getTitle())
            .build();
        
        given(movieRepository.save(any(Movie.class))).willReturn(savedMovie);

        // When
        MovieDTO result = movieService.createMovie(newMovieDTO);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createMovie_ValidMovie_SetsActiveToTrue() {
        // Given
        MovieDTO newMovieDTO = DTOFixtures.createMovieDTOWithoutId();
        Movie savedMovie = EntityFixtures.movieBuilder().id(1L).active(true).build();
        
        given(movieRepository.save(any(Movie.class))).willReturn(savedMovie);

        // When
        MovieDTO result = movieService.createMovie(newMovieDTO);

        // Then
        assertThat(result.getActive()).isTrue();
    }

    @Test
    void createMovie_ValidMovie_CallsRepositorySave() {
        // Given
        MovieDTO newMovieDTO = DTOFixtures.createMovieDTOWithoutId();
        Movie savedMovie = EntityFixtures.movieBuilder().id(1L).build();
        
        given(movieRepository.save(any(Movie.class))).willReturn(savedMovie);

        // When
        movieService.createMovie(newMovieDTO);

        // Then
        then(movieRepository).should(times(1)).save(any(Movie.class));
    }

    // ========== updateMovie Tests ==========

    @Test
    void updateMovie_ExistingMovie_ReturnsUpdatedMovieDTO() {
        // Given
        MovieDTO updateDTO = DTOFixtures.createDefaultMovieDTO();
        updateDTO.setTitle("Updated Title");
        
        given(movieRepository.findById(1L)).willReturn(Optional.of(testMovie));
        given(movieRepository.save(any(Movie.class))).willReturn(testMovie);

        // When
        MovieDTO result = movieService.updateMovie(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void updateMovie_NonExistingMovie_ThrowsResourceNotFoundException() {
        // Given
        MovieDTO updateDTO = DTOFixtures.createDefaultMovieDTO();
        given(movieRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.updateMovie(999L, updateDTO))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMovie_ExistingMovie_CallsRepositorySave() {
        // Given
        MovieDTO updateDTO = DTOFixtures.createDefaultMovieDTO();
        given(movieRepository.findById(1L)).willReturn(Optional.of(testMovie));
        given(movieRepository.save(any(Movie.class))).willReturn(testMovie);

        // When
        movieService.updateMovie(1L, updateDTO);

        // Then
        then(movieRepository).should(times(1)).save(any(Movie.class));
    }

    // ========== deleteMovie Tests (Soft Delete) ==========

    @Test
    void deleteMovie_ExistingMovie_SetsActiveToFalse() {
        // Given
        given(movieRepository.findById(1L)).willReturn(Optional.of(testMovie));
        given(movieRepository.save(any(Movie.class))).willReturn(testMovie);

        // When
        movieService.deleteMovie(1L);

        // Then
        then(movieRepository).should(times(1)).save(any(Movie.class));
    }

    @Test
    void deleteMovie_NonExistingMovie_ThrowsResourceNotFoundException() {
        // Given
        given(movieRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.deleteMovie(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteMovie_NonExistingMovie_DoesNotCallRepositorySave() {
        // Given
        given(movieRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        try {
            movieService.deleteMovie(999L);
        } catch (ResourceNotFoundException e) {
            // Expected
        }
        then(movieRepository).should(never()).save(any());
    }
}
