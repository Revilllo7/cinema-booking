package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Booking;
import com.cinema.entity.Screening;
import com.cinema.entity.User;
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
 * Integration tests for BookingRepository using H2 in-memory database.
 * Tests CRUD operations, custom query methods, and pagination for bookings.
 */
@DataJpaTest
class BookingRepositoryTest extends PostgresTestContainer {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidBooking_PersistsToDatabase() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build();

        // When
        Booking saved = bookingRepository.save(booking);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBookingNumber()).isNotNull();
    }

    @Test
    void save_BookingWithMultipleStatuses_PerformsCRUD() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.PENDING)
            .build();

        // When
        Booking saved = bookingRepository.save(booking);
        entityManager.flush();

        // Then
        assertThat(saved.getStatus()).isEqualTo(Booking.BookingStatus.PENDING);
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingBooking_ReturnsBooking() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        entityManager.flush();

        // When
        Optional<Booking> found = bookingRepository.findById(booking.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(booking.getId());
    }

    @Test
    void findById_NonExistingBooking_ReturnsEmpty() {
        // When
        Optional<Booking> found = bookingRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByBookingNumber_ExistingNumber_ReturnsBooking() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        String bookingNumber = "BOOK-123456";
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .bookingNumber(bookingNumber)
            .build());
        entityManager.flush();

        // When
        Optional<Booking> found = bookingRepository.findByBookingNumber(bookingNumber);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(booking.getId());
        assertThat(found.get().getBookingNumber()).isEqualTo(bookingNumber);
    }

    @Test
    void findByBookingNumber_NonExistingNumber_ReturnsEmpty() {
        // When
        Optional<Booking> found = bookingRepository.findByBookingNumber("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_WithUserBookings_ReturnsPagedResults() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie1 = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall1 = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow1 = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening1 = entityManager.persist(Screening.builder()
            .movie(movie1)
            .hall(hall1)
            .startTime(tomorrow1)
            .endTime(tomorrow1.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        var movie2 = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall2 = entityManager.persist(EntityFixtures.createLargeHall());
        LocalDateTime tomorrow2 = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0);
        Screening screening2 = entityManager.persist(Screening.builder()
            .movie(movie2)
            .hall(hall2)
            .startTime(tomorrow2)
            .endTime(tomorrow2.plusHours(2))
            .basePrice(30.0)
            .active(true)
            .build());

        entityManager.persist(EntityFixtures.bookingBuilder().user(user).screening(screening1).build());
        entityManager.persist(EntityFixtures.bookingBuilder().user(user).screening(screening2).build());
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Booking> result = bookingRepository.findByUserId(user.getId(), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(b -> b.getUser().getId().equals(user.getId()));
    }

    @Test
    void findByScreeningId_WithScreeningBookings_ReturnsAllBookings() {
        // Given
        User user1 = entityManager.persist(EntityFixtures.userBuilder().username("user1").email("user1@test.com").build());
        User user2 = entityManager.persist(EntityFixtures.userBuilder().username("user2").email("user2@test.com").build());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        entityManager.persist(EntityFixtures.bookingBuilder().user(user1).screening(screening).build());
        entityManager.persist(EntityFixtures.bookingBuilder().user(user2).screening(screening).build());
        entityManager.flush();

        // When
        List<Booking> result = bookingRepository.findByScreeningId(screening.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.getScreening().getId().equals(screening.getId()));
    }

    @Test
    void findByStatus_WithConfirmedBookings_ReturnsMatchingBookings() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.CONFIRMED)
            .build());
        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.PENDING)
            .build());
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Booking> result = bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
    }

    @Test
    void findByCreatedAtBetween_WithDateRange_ReturnsMatchingBookings() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime screeningTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(screeningTime)
            .endTime(screeningTime.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        // Note: @CreatedDate auto-sets createdAt, so we can't control it directly
        // Both bookings will have similar timestamps. This test needs to be adjusted.
        Booking booking1 = EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build();
        entityManager.persist(booking1);
        entityManager.flush();
        
        // Manually update createdAt in database to test date range filtering
        entityManager.getEntityManager().createQuery(
            "UPDATE Booking b SET b.createdAt = :oldDate WHERE b.id = :id")
            .setParameter("oldDate", now.minusDays(10))
            .setParameter("id", booking1.getId())
            .executeUpdate();
        
        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        List<Booking> result = bookingRepository.findByCreatedAtBetween(yesterday, tomorrow);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void countConfirmedBookingsByScreeningId_WithConfirmedBookings_ReturnsCount() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.CONFIRMED)
            .build());
        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.CONFIRMED)
            .build());
        entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.PENDING)
            .build());
        entityManager.flush();

        // When
        Long count = bookingRepository.countConfirmedBookingsByScreeningId(screening.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingBooking_PersistsChanges() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .status(Booking.BookingStatus.PENDING)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        Booking toUpdate = bookingRepository.findById(booking.getId()).orElseThrow();
        toUpdate.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingBooking_RemovesFromDatabase() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        entityManager.flush();
        Long bookingId = booking.getId();

        // When
        bookingRepository.deleteById(bookingId);
        entityManager.flush();

        // Then
        assertThat(bookingRepository.findById(bookingId)).isEmpty();
    }

    @Test
    void existsById_ExistingBooking_ReturnsTrue() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        entityManager.flush();

        // When
        boolean exists = bookingRepository.existsById(booking.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingBooking_ReturnsFalse() {
        // When
        boolean exists = bookingRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(EntityFixtures.bookingBuilder()
                .user(user)
                .screening(screening)
                .bookingNumber("BOOK-" + i)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Booking> page = bookingRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(EntityFixtures.createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());

        for (int i = 0; i < 15; i++) {
            entityManager.persist(EntityFixtures.bookingBuilder()
                .user(user)
                .screening(screening)
                .bookingNumber("BOOK-" + i)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<Booking> page = bookingRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }
}
