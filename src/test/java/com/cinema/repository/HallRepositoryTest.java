package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Hall;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HallRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for cinema halls.
 */
@DataJpaTest
class HallRepositoryTest extends PostgresTestContainer {

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidHall_PersistsToDatabase() {
        // Given
        Hall hall = Hall.builder()
            .name("Hall 1")
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build();

        // When
        Hall saved = hallRepository.save(hall);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Hall 1");
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingHall_ReturnsHall() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.flush();

        // When
        Optional<Hall> found = hallRepository.findById(hall.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(hall.getName());
    }

    @Test
    void findById_NonExistingHall_ReturnsEmpty() {
        // When
        Optional<Hall> found = hallRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ExistingName_ReturnsHall() {
        // Given
        Hall hall = Hall.builder()
            .name("Premium Hall")
            .totalSeats(150)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build();
        entityManager.persist(hall);
        entityManager.flush();

        // When
        Optional<Hall> found = hallRepository.findByName("Premium Hall");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Premium Hall");
    }

    @Test
    void findByName_NonExistingName_ReturnsEmpty() {
        // When
        Optional<Hall> found = hallRepository.findByName("NonExistent Hall");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByActiveTrueOrderByName_WithActiveHalls_ReturnsOrderedResults() {
        // Given
        entityManager.persist(Hall.builder().name("Zebra Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.persist(Hall.builder().name("Alpha Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.persist(Hall.builder().name("Beta Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).active(false).build());
        entityManager.flush();

        // When
        List<Hall> result = hallRepository.findByActiveTrueOrderByName();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Alpha Hall");
        assertThat(result.get(1).getName()).isEqualTo("Zebra Hall");
    }

    @Test
    void findByIdAndActiveTrue_ActiveHall_ReturnsHall() {
        // Given
        Hall hall = entityManager.persist(Hall.builder()
            .name("Active Hall")
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        entityManager.flush();

        // When
        Optional<Hall> found = hallRepository.findByIdAndActiveTrue(hall.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActive()).isTrue();
    }

    @Test
    void findByIdAndActiveTrue_InactiveHall_ReturnsEmpty() {
        // Given
        Hall hall = entityManager.persist(Hall.builder()
            .name("Inactive Hall")
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(false)
            .build());
        entityManager.flush();

        // When
        Optional<Hall> found = hallRepository.findByIdAndActiveTrue(hall.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAvailableHallsWithMinSeats_WithSufficientSeats_ReturnsHalls() {
        // Given
        entityManager.persist(Hall.builder().name("Small Hall").totalSeats(50).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.persist(Hall.builder().name("Medium Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.persist(Hall.builder().name("Large Hall").totalSeats(200).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.persist(Hall.builder().name("Inactive Large").totalSeats(200).rowsCount(10).seatsPerRow(10).active(false).build());
        entityManager.flush();

        // When
        List<Hall> result = hallRepository.findAvailableHallsWithMinSeats(150);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Large Hall");
    }

    @Test
    void findAvailableHallsWithMinSeats_NoHallsMeetCriteria_ReturnsEmptyList() {
        // Given
        entityManager.persist(Hall.builder().name("Small Hall").totalSeats(50).rowsCount(10).seatsPerRow(10).active(true).build());
        entityManager.flush();

        // When
        List<Hall> result = hallRepository.findAvailableHallsWithMinSeats(100);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingHall_PersistsChanges() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.flush();
        entityManager.clear();

        // When
        Hall toUpdate = hallRepository.findById(hall.getId()).orElseThrow();
        toUpdate.setName("Updated Hall Name");
        toUpdate.setTotalSeats(150);
        Hall updated = hallRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Hall Name");
        assertThat(updated.getTotalSeats()).isEqualTo(150);
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingHall_RemovesFromDatabase() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.flush();
        Long hallId = hall.getId();

        // When
        hallRepository.deleteById(hallId);
        entityManager.flush();

        // Then
        assertThat(hallRepository.findById(hallId)).isEmpty();
    }

    @Test
    void existsById_ExistingHall_ReturnsTrue() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.flush();

        // When
        boolean exists = hallRepository.existsById(hall.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingHall_ReturnsFalse() {
        // When
        boolean exists = hallRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Hall.builder()
                .name("Hall " + i)
                .totalSeats(100 + i * 10)                .rowsCount(10)
                .seatsPerRow(10)                .rowsCount(10)
                .seatsPerRow(10)
                .active(true)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<Hall> page = hallRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Hall.builder()
                .name("Hall " + i)
                .totalSeats(100 + i * 10)
                .rowsCount(10)
                .seatsPerRow(10)
                .active(true)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(1, 10);

        // When
        org.springframework.data.domain.Page<Hall> page = hallRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
