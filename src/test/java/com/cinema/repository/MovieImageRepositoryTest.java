package com.cinema.repository;

import com.cinema.entity.Movie;
import com.cinema.entity.MovieImage;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MovieImageRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for movie images.
 */
@DataJpaTest
class MovieImageRepositoryTest extends PostgresTestContainer {

    @Autowired
    private MovieImageRepository movieImageRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidMovieImage_PersistsToDatabase() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        MovieImage image = MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build();

        // When
        MovieImage saved = movieImageRepository.save(image);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getImagePath()).isEqualTo("/images/image1.jpg");
    }

    @Test
    void save_MultipleImagesForMovie_PersistAll() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());

        MovieImage image1 = MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build();
        MovieImage image2 = MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image2.jpg")
            .displayOrder(2)
            .build();
        MovieImage image3 = MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image3.jpg")
            .displayOrder(3)
            .build();

        // When
        movieImageRepository.save(image1);
        movieImageRepository.save(image2);
        movieImageRepository.save(image3);
        entityManager.flush();

        // Then
        List<MovieImage> saved = movieImageRepository.findByMovieIdOrderByDisplayOrder(movie.getId());
        assertThat(saved).hasSize(3);
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingImage_ReturnsImage() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        MovieImage image = entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.flush();

        // When
        Optional<MovieImage> found = movieImageRepository.findById(image.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getImagePath()).isEqualTo("/images/image1.jpg");
    }

    @Test
    void findById_NonExistingImage_ReturnsEmpty() {
        // When
        Optional<MovieImage> found = movieImageRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByMovieIdOrderByDisplayOrder_WithMultipleImages_ReturnsOrderedList() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());

        entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image3.jpg")
            .displayOrder(3)
            .build());
        entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image2.jpg")
            .displayOrder(2)
            .build());
        entityManager.flush();

        // When
        List<MovieImage> result = movieImageRepository.findByMovieIdOrderByDisplayOrder(movie.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
        assertThat(result.get(2).getDisplayOrder()).isEqualTo(3);
    }

    @Test
    void findByMovieIdOrderByDisplayOrder_NoImages_ReturnsEmptyList() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        entityManager.flush();

        // When
        List<MovieImage> result = movieImageRepository.findByMovieIdOrderByDisplayOrder(movie.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByMovieIdOrderByDisplayOrder_MultipleMovies_ReturnsOnlyForMovie() {
        // Given
        Movie movie1 = entityManager.persist(EntityFixtures.createDefaultMovie());
        Movie movie2 = entityManager.persist(EntityFixtures.movieBuilder()
            .title("Another Movie")
            .build());

        entityManager.persist(MovieImage.builder()
            .movie(movie1)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.persist(MovieImage.builder()
            .movie(movie2)
            .imagePath("/images/image2.jpg")
            .displayOrder(1)
            .build());
        entityManager.flush();

        // When
        List<MovieImage> result = movieImageRepository.findByMovieIdOrderByDisplayOrder(movie1.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovie().getId()).isEqualTo(movie1.getId());
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingImage_PersistsChanges() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        MovieImage image = entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/old.jpg")
            .displayOrder(1)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        MovieImage toUpdate = movieImageRepository.findById(image.getId()).orElseThrow();
        toUpdate.setImagePath("/images/new.jpg");
        toUpdate.setDisplayOrder(2);
        MovieImage updated = movieImageRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getImagePath()).isEqualTo("/images/new.jpg");
        assertThat(updated.getDisplayOrder()).isEqualTo(2);
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingImage_RemovesFromDatabase() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        MovieImage image = entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.flush();
        Long imageId = image.getId();

        // When
        movieImageRepository.deleteById(imageId);
        entityManager.flush();

        // Then
        assertThat(movieImageRepository.findById(imageId)).isEmpty();
    }

    @Test
    void deleteByMovieId_WithMultipleImages_RemovesAllImages() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());

        entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image2.jpg")
            .displayOrder(2)
            .build());
        entityManager.flush();

        // When
        movieImageRepository.deleteByMovieId(movie.getId());
        entityManager.flush();

        // Then
        List<MovieImage> remaining = movieImageRepository.findByMovieIdOrderByDisplayOrder(movie.getId());
        assertThat(remaining).isEmpty();
    }

    @Test
    void existsById_ExistingImage_ReturnsTrue() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        MovieImage image = entityManager.persist(MovieImage.builder()
            .movie(movie)
            .imagePath("/images/image1.jpg")
            .displayOrder(1)
            .build());
        entityManager.flush();

        // When
        boolean exists = movieImageRepository.existsById(image.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingImage_ReturnsFalse() {
        // When
        boolean exists = movieImageRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(MovieImage.builder()
                .movie(movie)
                .imagePath("/images/image" + i + ".jpg")
                .displayOrder(i)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<MovieImage> page = movieImageRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        Movie movie = entityManager.persist(EntityFixtures.createDefaultMovie());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(MovieImage.builder()
                .movie(movie)
                .imagePath("/images/image" + i + ".jpg")
                .displayOrder(i)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(1, 10);

        // When
        org.springframework.data.domain.Page<MovieImage> page = movieImageRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
