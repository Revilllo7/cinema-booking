package com.cinema.repository.jdbc;

import com.cinema.entity.Booking;
import com.cinema.entity.Movie;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.TicketType;
import com.cinema.entity.User;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.support.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ReportingDao and BookingJdbcRepository using PostgreSQL test container.
 * Tests JDBC operations for reporting and sales analysis with real SQL queries.
 */
@DataJpaTest
class ReportingDaoTest extends PostgresTestContainer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    private ReportingDao reportingDao;
    private Movie testMovie;
    private User testUser;
    private Screening testScreening;
    private TicketType ticketType;
    private LocalDateTime now;
    private long seatSequence = 1L;

    @BeforeEach
    void setUp() {
        reportingDao = new ReportingDao(jdbcTemplate);

        now = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0);

        testUser = entityManager.persist(EntityFixtures.createDefaultUser());
        testMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        testScreening = Screening.builder()
            .movie(testMovie)
            .hall(entityManager.persist(EntityFixtures.createDefaultHall()))
            .startTime(now.plusDays(1))
            .endTime(now.plusDays(1).plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build();
        entityManager.persist(testScreening);
        entityManager.flush();
    }

    // ========== Total Revenue Tests ==========

    @Test
    @DisplayName("Should return zero revenue when no confirmed bookings exist")
    void totalRevenueBetween_NoBookings_ReturnsZero() {
        LocalDateTime start = now;
        LocalDateTime end = now.plusDays(1);

        BigDecimal revenue = reportingDao.totalRevenueBetween(start, end);

        assertThat(revenue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should sum revenue from confirmed bookings")
    void totalRevenueBetween_ConfirmedBookings_SumsTotalPrice() {
        createAndPersistBooking(now, 50.0, Booking.BookingStatus.CONFIRMED);
        createAndPersistBooking(now.plusHours(1), 75.0, Booking.BookingStatus.CONFIRMED);

        BigDecimal revenue = reportingDao.totalRevenueBetween(now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Should sum revenue from completed bookings")
    void totalRevenueBetween_CompletedBookings_SumsTotalPrice() {
        createAndPersistBooking(now, 100.0, Booking.BookingStatus.COMPLETED);

        BigDecimal revenue = reportingDao.totalRevenueBetween(now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should exclude pending bookings from revenue")
    void totalRevenueBetween_PendingBookings_ExcludedFromRevenue() {
        createAndPersistBooking(now, 50.0, Booking.BookingStatus.PENDING);
        createAndPersistBooking(now.plusHours(1), 100.0, Booking.BookingStatus.CONFIRMED);

        BigDecimal revenue = reportingDao.totalRevenueBetween(now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should exclude bookings outside time range")
    void totalRevenueBetween_OutsideRange_ExcludedFromRevenue() {
        createAndPersistBooking(now.minusDays(2), 100.0, Booking.BookingStatus.CONFIRMED);
        createAndPersistBooking(now, 50.0, Booking.BookingStatus.CONFIRMED);

        BigDecimal revenue = reportingDao.totalRevenueBetween(now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    // ========== Tickets Sold Tests ==========

    @Test
    @DisplayName("Should return zero tickets sold when no confirmed bookings")
    void ticketsSoldBetween_NoBookings_ReturnsZero() {
        int tickets = reportingDao.ticketsSoldBetween(now, now.plusDays(1));

        assertThat(tickets).isZero();
    }

    @Test
    @DisplayName("Should count booking seats for confirmed bookings")
    void ticketsSoldBetween_ConfirmedBookings_CountsSeats() {
        Booking booking = createAndPersistBooking(now, 50.0, Booking.BookingStatus.CONFIRMED);
        createBookingSeat(booking);
        createBookingSeat(booking);

        int tickets = reportingDao.ticketsSoldBetween(now.minusHours(1), now.plusDays(1));

        assertThat(tickets).isEqualTo(2);
    }

    @Test
    @DisplayName("Should exclude pending bookings from ticket count")
    void ticketsSoldBetween_PendingBookings_ExcludedFromCount() {
        Booking pending = createAndPersistBooking(now, 50.0, Booking.BookingStatus.PENDING);
        createBookingSeat(pending);

        Booking confirmed = createAndPersistBooking(now.plusHours(1), 75.0, Booking.BookingStatus.CONFIRMED);
        createBookingSeat(confirmed);
        createBookingSeat(confirmed);

        int tickets = reportingDao.ticketsSoldBetween(now.minusHours(1), now.plusDays(1));

        assertThat(tickets).isEqualTo(2);
    }

    // ========== Revenue by Movie Tests ==========

    @Test
    @DisplayName("Should return zero revenue for movie with no bookings")
    void revenueByMovie_NoBookings_ReturnsZero() {
        Movie otherMovie = entityManager.persist(EntityFixtures.createDefaultMovie());

        BigDecimal revenue = reportingDao.revenueByMovie(otherMovie.getId(), now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should sum revenue for specific movie")
    void revenueByMovie_WithBookings_SumsMovieRevenue() {
        createAndPersistBooking(now, 50.0, Booking.BookingStatus.CONFIRMED);
        createAndPersistBooking(now.plusHours(1), 75.0, Booking.BookingStatus.CONFIRMED);

        BigDecimal revenue = reportingDao.revenueByMovie(testMovie.getId(), now.minusHours(1), now.plusDays(1));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("125.00"));
    }

    @Test
    @DisplayName("Should exclude bookings for other movies")
    void revenueByMovie_OnlyCountsTargetMovie_ExcludesOthers() {
        Movie otherMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        Screening otherScreening = Screening.builder()
            .movie(otherMovie)
            .hall(testScreening.getHall())
            .startTime(now.plusDays(2))
            .endTime(now.plusDays(2).plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build();
        entityManager.persist(otherScreening);

        createAndPersistBooking(now, 100.0, Booking.BookingStatus.CONFIRMED);

        Booking otherBooking = Booking.builder()
            .user(testUser)
            .screening(otherScreening)
            .bookingNumber("OTHER-BK-1")
            .status(Booking.BookingStatus.CONFIRMED)
            .totalPrice(50.0)
            .createdAt(now.plusDays(2))
            .build();
        entityManager.persist(otherBooking);
        entityManager.flush();
        jdbcTemplate.update(
            "UPDATE bookings SET created_at = ?, updated_at = ? WHERE id = ?",
            Timestamp.valueOf(now.plusDays(2)),
            Timestamp.valueOf(now.plusDays(2)),
            otherBooking.getId()
        );

        BigDecimal revenue = reportingDao.revenueByMovie(testMovie.getId(), now.minusHours(1), now.plusDays(3));

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    // ========== Seats Booked Tests ==========

    @Test
    @DisplayName("Should return zero seats booked when no confirmed bookings")
    void seatsBookedForScreening_NoBookings_ReturnsZero() {
        int seats = reportingDao.seatsBookedForScreening(testScreening.getId());

        assertThat(seats).isZero();
    }

    @Test
    @DisplayName("Should count booking seats for screening")
    void seatsBookedForScreening_WithBookings_CountsSeats() {
        Booking booking = createAndPersistBooking(now, 50.0, Booking.BookingStatus.CONFIRMED);
        createBookingSeat(booking);
        createBookingSeat(booking);

        int seats = reportingDao.seatsBookedForScreening(testScreening.getId());

        assertThat(seats).isEqualTo(2);
    }

    @Test
    @DisplayName("Should exclude pending bookings from seat count")
    void seatsBookedForScreening_ExcludesPendingBookings_CountsOnlyConfirmed() {
        Booking pending = createAndPersistBooking(now, 50.0, Booking.BookingStatus.PENDING);
        createBookingSeat(pending);

        Booking confirmed = createAndPersistBooking(now.plusHours(1), 75.0, Booking.BookingStatus.CONFIRMED);
        createBookingSeat(confirmed);
        createBookingSeat(confirmed);

        int seats = reportingDao.seatsBookedForScreening(testScreening.getId());

        assertThat(seats).isEqualTo(2);
    }

    // ========== Helper Methods ==========

    private Booking createAndPersistBooking(LocalDateTime createdAt, double totalPrice, Booking.BookingStatus status) {
        Booking booking = Booking.builder()
            .user(testUser)
            .screening(testScreening)
            .bookingNumber("BK-" + System.nanoTime())
            .status(status)
            .totalPrice(totalPrice)
            .createdAt(createdAt)
            .build();
        entityManager.persist(booking);
        entityManager.flush();
        jdbcTemplate.update(
            "UPDATE bookings SET created_at = ?, updated_at = ? WHERE id = ?",
            Timestamp.valueOf(createdAt),
            Timestamp.valueOf(createdAt),
            booking.getId()
        );
        return booking;
    }

    private void createBookingSeat(Booking booking) {
        String insertSql = "INSERT INTO booking_seats (booking_id, seat_id, ticket_type_id, price, seat_status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
            insertSql,
            booking.getId(),
            persistSeatId(),
            ticketType.getId(),
            booking.getTotalPrice() != null ? booking.getTotalPrice() : 25.0,
            "RESERVED"
        );
    }

    private Long persistSeatId() {
        long current = seatSequence++;
        int row = (int) ((current - 1) / 10) + 1;
        int number = (int) ((current - 1) % 10) + 1;
        Seat seatEntity = Seat.builder()
            .hall(testScreening.getHall())
            .rowNumber(row)
            .seatNumber(number)
            .seatType(Seat.SeatType.STANDARD)
            .build();
        entityManager.persist(seatEntity);
        entityManager.flush();
        return seatEntity.getId();
    }
}
