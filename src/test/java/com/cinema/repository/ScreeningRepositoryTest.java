package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Screening;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ScreeningRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for movie screenings.
 */
@DataJpaTest
class ScreeningRepositoryTest extends PostgresTestContainer {

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidScreening_PersistsToDatabase() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);

        Screening screening = Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build();

        // When
        Screening saved = screeningRepository.save(screening);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBasePrice()).isEqualTo(25.0);
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingScreening_ReturnsScreening() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
            .endTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // When
        Optional<Screening> found = screeningRepository.findById(screening.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(screening.getId());
    }

    @Test
    void findById_NonExistingScreening_ReturnsEmpty() {
        // When
        Optional<Screening> found = screeningRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByIdAndActiveTrue_ActiveScreening_ReturnsScreening() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
            .endTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // When
        Optional<Screening> found = screeningRepository.findByIdAndActiveTrue(screening.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActive()).isTrue();
    }

    @Test
    void findByIdAndActiveTrue_InactiveScreening_ReturnsEmpty() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1))
            .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
            .basePrice(25.0)
            .active(false)
            .build());
        entityManager.flush();

        // When
        Optional<Screening> found = screeningRepository.findByIdAndActiveTrue(screening.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findUpcomingScreenings_WithFutureScreenings_ReturnsPagedResults() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        var pastMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var pastHall = entityManager.persist(EntityFixtures.createLargeHall());
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        entityManager.persist(Screening.builder()
            .movie(pastMovie)
            .hall(pastHall)
            .startTime(yesterday)
            .endTime(yesterday.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Screening> result = screeningRepository.findUpcomingScreenings(LocalDateTime.now(), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStartTime()).isAfter(LocalDateTime.now());
    }

    @Test
    void findByMovieIdAndStartTimeAfter_WithFutureScreenings_ReturnsMatchingScreenings() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        var pastMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var pastHall = entityManager.persist(EntityFixtures.createLargeHall());
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(18).withMinute(0);
        entityManager.persist(Screening.builder()
            .movie(pastMovie)
            .hall(pastHall)
            .startTime(yesterday)
            .endTime(yesterday.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // When
        List<Screening> result = screeningRepository.findByMovieIdAndStartTimeAfter(
            movie.getId(), 
            LocalDateTime.now()
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovie().getId()).isEqualTo(movie.getId());
    }

    @Test
    void findByHallIdAndStartTimeBetween_WithTimeRange_ReturnsMatchingScreenings() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18);
        LocalDateTime afterTomorrow = LocalDateTime.now().plusDays(2).withHour(18);

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(afterTomorrow)
            .endTime(afterTomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        entityManager.flush();

        LocalDateTime startRange = LocalDateTime.now();
        LocalDateTime endRange = LocalDateTime.now().plusDays(2);

        // When
        List<Screening> result = screeningRepository.findByHallIdAndStartTimeBetween(
            hall.getId(), 
            startRange, 
            endRange
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getHall().getId().equals(hall.getId()));
    }

    @Test
    void findByDate_WithSpecificDate_ReturnsScreeningsForDate() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        var anotherMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var anotherHall = entityManager.persist(EntityFixtures.createLargeHall());
        entityManager.persist(Screening.builder()
            .movie(anotherMovie)
            .hall(anotherHall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // When
        List<Screening> result = screeningRepository.findByDate(tomorrow);

        // Then - Now expecting 2 screenings on the same date (both have same tomorrow date)
        assertThat(result).hasSize(2);
    }

    @Test
    void existsConflictingScreening_WithConflict_ReturnsTrue() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(2);

        entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(startTime)
            .endTime(endTime)
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // Test with a time range that overlaps with the existing screening
        // Existing: 18:00 - 20:00
        // New: 18:30 - 20:30 (overlaps)
        LocalDateTime conflictStart = startTime.plusMinutes(30);  // 18:30
        LocalDateTime conflictEnd = conflictStart.plusHours(2);    // 20:30

        // When - use a different screening ID to simulate checking for conflicts
        boolean exists = screeningRepository.existsConflictingScreening(
            hall.getId(), 
            -1L,  // Use -1 to ensure we're not excluding the existing screening
            conflictStart, 
            conflictEnd
        );

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsConflictingScreening_NoConflict_ReturnsFalse() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18);
        LocalDateTime endTime = startTime.plusHours(2);

        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(startTime)
            .endTime(endTime)
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        LocalDateTime noConflictStart = startTime.plusHours(3);
        LocalDateTime noConflictEnd = noConflictStart.plusHours(2);

        // When
        boolean exists = screeningRepository.existsConflictingScreening(
            hall.getId(), 
            screening.getId(), 
            noConflictStart, 
            noConflictEnd
        );

        // Then
        assertThat(exists).isFalse();
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingScreening_PersistsChanges() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
            .endTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        Screening toUpdate = screeningRepository.findById(screening.getId()).orElseThrow();
        toUpdate.setBasePrice(30.0);
        toUpdate.setActive(false);
        Screening updated = screeningRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getBasePrice()).isEqualTo(30.0);
        assertThat(updated.getActive()).isFalse();
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingScreening_RemovesFromDatabase() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
            .endTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();
        Long screeningId = screening.getId();

        // When
        screeningRepository.deleteById(screeningId);
        entityManager.flush();

        // Then
        assertThat(screeningRepository.findById(screeningId)).isEmpty();
    }

    @Test
    void existsById_ExistingScreening_ReturnsTrue() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0))
            .endTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0))
            .basePrice(25.0)
            .active(true)
            .build());
        entityManager.flush();

        // When
        boolean exists = screeningRepository.existsById(screening.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingScreening_ReturnsFalse() {
        // When
        boolean exists = screeningRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());

        for (int i = 0; i < 15; i++) {
            LocalDateTime screeningTime = LocalDateTime.now().plusDays(i);
            entityManager.persist(Screening.builder()
                .movie(movie)
                .hall(hall)
                .startTime(screeningTime)
                .endTime(screeningTime.plusHours(2))
                .basePrice(25.0)
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Screening> page = screeningRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());

        for (int i = 0; i < 15; i++) {
            LocalDateTime screeningTime = LocalDateTime.now().plusDays(i);
            entityManager.persist(Screening.builder()
                .movie(movie)
                .hall(hall)
                .startTime(screeningTime)
                .endTime(screeningTime.plusHours(2))
                .basePrice(25.0)
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<Screening> page = screeningRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
