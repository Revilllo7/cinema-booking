package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Hall;
import com.cinema.entity.Seat;
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
 * Integration tests for SeatRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for cinema seats.
 */
@DataJpaTest
class SeatRepositoryTest extends PostgresTestContainer {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidSeat_PersistsToDatabase() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build();

        // When
        Seat saved = seatRepository.save(seat);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRowNumber()).isEqualTo(5);
        assertThat(saved.getSeatNumber()).isEqualTo(10);
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingSeat_ReturnsSeat() {
        // Given
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.flush();

        // When
        Optional<Seat> found = seatRepository.findById(seat.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(seat.getId());
    }

    @Test
    void findById_NonExistingSeat_ReturnsEmpty() {
        // When
        Optional<Seat> found = seatRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByHallIdAndActiveTrue_WithActiveSeats_ReturnsOrderedSeats() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());

        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(2)
            .seatNumber(5)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(5)
            .seatType(Seat.SeatType.STANDARD)
            .active(false)
            .build());
        entityManager.flush();

        // When
        List<Seat> result = seatRepository.findByHallIdAndActiveTrue(hall.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRowNumber()).isEqualTo(1);
        assertThat(result.get(1).getRowNumber()).isEqualTo(2);
    }

    @Test
    void findByHallIdAndActiveTrue_InactiveSeats_ReturnsEmptyList() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(5)
            .seatType(Seat.SeatType.STANDARD)
            .active(false)
            .build());
        entityManager.flush();

        // When
        List<Seat> result = seatRepository.findByHallIdAndActiveTrue(hall.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByHallIdAndRowAndSeat_ExistingSeat_ReturnsSeat() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.flush();

        // When
        Optional<Seat> found = seatRepository.findByHallIdAndRowAndSeat(hall.getId(), 5, 10);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(seat.getId());
    }

    @Test
    void findByHallIdAndRowAndSeat_NonExistingSeat_ReturnsEmpty() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());
        entityManager.flush();

        // When
        Optional<Seat> found = seatRepository.findByHallIdAndRowAndSeat(hall.getId(), 99, 99);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByHallIdAndSeatType_WithVIPSeats_ReturnsVIPSeats() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());

        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(1)
            .seatType(Seat.SeatType.VIP)
            .active(true)
            .build());
        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(2)
            .seatType(Seat.SeatType.VIP)
            .active(true)
            .build());
        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(2)
            .seatNumber(1)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.flush();

        // When
        List<Seat> result = seatRepository.findByHallIdAndSeatType(hall.getId(), Seat.SeatType.VIP);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getSeatType() == Seat.SeatType.VIP);
    }

    @Test
    void findByHallIdAndSeatType_InactiveSeats_ReturnsEmptyList() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());

        entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(1)
            .seatType(Seat.SeatType.VIP)
            .active(false)
            .build());
        entityManager.flush();

        // When
        List<Seat> result = seatRepository.findByHallIdAndSeatType(hall.getId(), Seat.SeatType.VIP);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingSeat_PersistsChanges() {
        // Given
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        Seat toUpdate = seatRepository.findById(seat.getId()).orElseThrow();
        toUpdate.setSeatType(Seat.SeatType.VIP);
        toUpdate.setActive(false);
        Seat updated = seatRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getSeatType()).isEqualTo(Seat.SeatType.VIP);
        assertThat(updated.getActive()).isFalse();
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingSeat_RemovesFromDatabase() {
        // Given
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        entityManager.flush();
        Long seatId = seat.getId();

        // When
        seatRepository.deleteById(seatId);
        entityManager.flush();

        // Then
        assertThat(seatRepository.findById(seatId)).isEmpty();
    }

    @Test
    void existsById_ExistingSeat_ReturnsTrue() {
        // Given
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(1)
            .seatNumber(1)
            .seatType(Seat.SeatType.VIP)
            .active(true)
            .build());
        entityManager.flush();

        // When
        boolean exists = seatRepository.existsById(seat.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingSeat_ReturnsFalse() {
        // When
        boolean exists = seatRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(Seat.builder()
                .hall(hall)
                .rowNumber(i / 5)
                .seatNumber(i % 5)
                .seatType(Seat.SeatType.STANDARD)
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Seat> page = seatRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        Hall hall = entityManager.persist(EntityFixtures.createDefaultHall());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(Seat.builder()
                .hall(hall)
                .rowNumber(i / 5)
                .seatNumber(i % 5)
                .seatType(Seat.SeatType.STANDARD)
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<Seat> page = seatRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
