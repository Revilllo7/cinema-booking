package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Movie;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MovieRepository using H2 in-memory database.
 */
@DataJpaTest
class MovieRepositoryTest extends PostgresTestContainer {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidMovie_PersistsToDatabase() {
        // Given
        Movie movie = EntityFixtures.movieBuilder()
            .title("New Movie")
            .build();

        // When
        Movie saved = movieRepository.save(movie);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingMovie_ReturnsMovie() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();

        // When
        Optional<Movie> found = movieRepository.findById(movie.getId());

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findById_NonExistingMovie_ReturnsEmpty() {
        // When
        Optional<Movie> found = movieRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByIdAndActiveTrue_ActiveMovie_ReturnsMovie() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();

        // When
        Optional<Movie> found = movieRepository.findByIdAndActiveTrue(movie.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActive()).isTrue();
    }

    @Test
    void findByIdAndActiveTrue_InactiveMovie_ReturnsEmpty() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createInactiveMovie());
        entityManager.flush();

        // When
        Optional<Movie> found = movieRepository.findByIdAndActiveTrue(movie.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByActiveTrueOrderByCreatedAtDesc_WithActiveMovies_ReturnsOrderedResults() {
        // Given
        Movie movie1 = EntityFixtures.movieBuilder().title("Movie 1").build();
        Movie movie2 = EntityFixtures.movieBuilder().title("Movie 2").build();
        Movie inactiveMovie = EntityFixtures.createInactiveMovie();
        
        entityManager.persist(movie1);
        entityManager.persist(movie2);
        entityManager.persist(inactiveMovie);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Movie> result = movieRepository.findByActiveTrueOrderByCreatedAtDesc(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchByTitle_MatchingKeyword_ReturnsMatchingMovies() {
        // Given
        Movie movie1 = EntityFixtures.movieBuilder().title("Inception Deep").build();
        Movie movie2 = EntityFixtures.movieBuilder().title("Deep Space").build();
        Movie movie3 = EntityFixtures.movieBuilder().title("Other Movie").build();
        
        entityManager.persist(movie1);
        entityManager.persist(movie2);
        entityManager.persist(movie3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Movie> result = movieRepository.searchByTitle("Deep", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchByTitle_NoMatch_ReturnsEmptyPage() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Movie> result = movieRepository.searchByTitle("NonExistent", pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByGenre_MatchingGenre_ReturnsMovies() {
        // Given
        Movie sciFi1 = EntityFixtures.movieBuilder().title("Movie 1").genre("Sci-Fi").build();
        Movie sciFi2 = EntityFixtures.movieBuilder().title("Movie 2").genre("Sci-Fi").build();
        Movie drama = EntityFixtures.movieBuilder().title("Movie 3").genre("Drama").build();
        
        entityManager.persist(sciFi1);
        entityManager.persist(sciFi2);
        entityManager.persist(drama);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Movie> result = movieRepository.findByGenre("Sci-Fi", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findAllGenres_WithMovies_ReturnsDistinctGenres() {
        // Given
        entityManager.persist(EntityFixtures.movieBuilder().genre("Action").build());
        entityManager.persist(EntityFixtures.movieBuilder().genre("Action").build());
        entityManager.persist(EntityFixtures.movieBuilder().genre("Drama").build());
        entityManager.persist(EntityFixtures.movieBuilder().genre("Sci-Fi").build());
        entityManager.flush();

        // When
        List<String> genres = movieRepository.findAllGenres();

        // Then
        assertThat(genres).hasSize(3);
        assertThat(genres).containsExactlyInAnyOrder("Action", "Drama", "Sci-Fi");
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingMovie_PersistsChanges() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();
        entityManager.clear();

        // When
        Movie toUpdate = movieRepository.findById(movie.getId()).orElseThrow();
        toUpdate.setTitle("Updated Title");
        Movie updated = movieRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingMovie_RemovesFromDatabase() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();
        Long movieId = movie.getId();

        // When
        movieRepository.deleteById(movieId);
        entityManager.flush();

        // Then
        assertThat(movieRepository.findById(movieId)).isEmpty();
    }

    @Test
    void existsById_ExistingMovie_ReturnsTrue() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();

        // When
        boolean exists = movieRepository.existsById(movie.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingMovie_ReturnsFalse() {
        // When
        boolean exists = movieRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        for (int i = 0; i < 15; i++) {
            Movie movie = EntityFixtures.movieBuilder()
                .title("Movie " + i)
                .build();
            entityManager.persist(movie);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Movie> page = movieRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        for (int i = 0; i < 15; i++) {
            Movie movie = EntityFixtures.movieBuilder()
                .title("Movie " + i)
                .build();
            entityManager.persist(movie);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<Movie> page = movieRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
