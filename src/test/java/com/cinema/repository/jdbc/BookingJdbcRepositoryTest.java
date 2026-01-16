package com.cinema.repository.jdbc;

import com.cinema.dto.DailySalesDTO;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BookingJdbcRepository using PostgreSQL test container.
 * Tests daily sales aggregation queries and RowMapper functionality.
 */
@DataJpaTest
class BookingJdbcRepositoryTest extends PostgresTestContainer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    private BookingJdbcRepository bookingJdbcRepository;
    private Movie testMovie;
    private User testUser;
    private Screening testScreening;
    private TicketType ticketType;
    private LocalDateTime baseDateTime;
    private long seatSequence = 1L;

    @BeforeEach
    void setUp() {
        bookingJdbcRepository = new BookingJdbcRepository(jdbcTemplate);

        baseDateTime = LocalDateTime.of(2025, 6, 15, 12, 0, 0);

        testUser = entityManager.persist(EntityFixtures.createDefaultUser());
        testMovie = entityManager.persist(EntityFixtures.createDefaultMovie());
        ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        testScreening = Screening.builder()
            .movie(testMovie)
            .hall(entityManager.persist(EntityFixtures.createDefaultHall()))
            .startTime(baseDateTime.plusDays(1))
            .endTime(baseDateTime.plusDays(1).plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build();
        entityManager.persist(testScreening);
        entityManager.flush();
    }

    // ========== Get Daily Sales for Month Tests ==========

    @Test
    @DisplayName("Should return empty list when no bookings in month")
    void getDailySalesForMonth_NoBookings_ReturnsEmptyList() {
        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        assertThat(sales).isEmpty();
    }

    @Test
    @DisplayName("Should return daily sales aggregated by date for month")
    void getDailySalesForMonth_WithBookings_ReturnsAggregatedSales() {
        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.plusHours(3), 75.0, Booking.BookingStatus.CONFIRMED, 2);
        createAndPersistBooking(baseDateTime.plusDays(1), 100.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        assertThat(sales).isNotEmpty();
        assertThat(sales.get(0).getDate()).isEqualTo(baseDateTime.toLocalDate());
        assertThat(sales.get(0).getTotalRevenue()).isCloseTo(125.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(sales.get(0).getTicketsSold()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should aggregate multiple bookings from same day")
    void getDailySalesForMonth_MultipleSameDay_AggregatesTotals() {
        LocalDate targetDate = baseDateTime.toLocalDate();

        createAndPersistBooking(baseDateTime.withHour(9).withMinute(0), 30.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.withHour(14).withMinute(0), 45.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.withHour(18).withMinute(0), 25.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        DailySalesDTO dailySales = sales.stream()
            .filter(s -> s.getDate().equals(targetDate))
            .findFirst()
            .orElseThrow();

        assertThat(dailySales.getTotalRevenue()).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(dailySales.getTicketsSold()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should include completed bookings in sales")
    void getDailySalesForMonth_CompletedBookings_IncludedInSales() {
        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.COMPLETED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        assertThat(sales).isNotEmpty();
        assertThat(sales.get(0).getTotalRevenue()).isCloseTo(50.0, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should include pending bookings in sales")
    void getDailySalesForMonth_PendingBookings_IncludedInSales() {
        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.PENDING, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        assertThat(sales).isNotEmpty();
        assertThat(sales.get(0).getTotalRevenue()).isCloseTo(50.0, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should only include bookings from specified month")
    void getDailySalesForMonth_DifferentMonths_OnlyIncludesTarget() {
        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.minusMonths(1), 100.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.plusMonths(1), 75.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        double totalRevenue = sales.stream().mapToDouble(DailySalesDTO::getTotalRevenue).sum();
        assertThat(totalRevenue).isCloseTo(50.0, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("Should sort results by date ascending")
    void getDailySalesForMonth_MultipleDays_SortsByDate() {
        LocalDateTime date1 = LocalDateTime.of(2025, 6, 10, 12, 0);
        LocalDateTime date3 = LocalDateTime.of(2025, 6, 20, 12, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 6, 15, 12, 0);

        // Create bookings out of order
        createAndPersistBooking(date3, 30.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(date1, 40.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(date2, 50.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(2025, 6);

        assertThat(sales).hasSize(3);
        assertThat(sales.get(0).getDate()).isEqualTo(date1.toLocalDate());
        assertThat(sales.get(1).getDate()).isEqualTo(date2.toLocalDate());
        assertThat(sales.get(2).getDate()).isEqualTo(date3.toLocalDate());
    }

    // ========== Get Daily Sales for Date Range Tests ==========

    @Test
    @DisplayName("Should return empty list when no bookings in range")
    void getDailySalesForDateRange_NoBookings_ReturnsEmptyList() {
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 10);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);

        assertThat(sales).isEmpty();
    }

    @Test
    @DisplayName("Should return daily sales for specified date range")
    void getDailySalesForDateRange_WithBookings_ReturnsSalesInRange() {
        LocalDate startDate = LocalDate.of(2025, 6, 10);
        LocalDate endDate = LocalDate.of(2025, 6, 20);

        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(baseDateTime.plusDays(1), 75.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);

        assertThat(sales).hasSize(2);
    }

    @Test
    @DisplayName("Should include start date in range")
    void getDailySalesForDateRange_StartDateInclusive_IncludesStartDate() {
        LocalDate startDate = baseDateTime.toLocalDate();
        LocalDate endDate = startDate.plusDays(5);

        createAndPersistBooking(baseDateTime.withHour(9).withMinute(0), 50.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);

        assertThat(sales).isNotEmpty();
        assertThat(sales.get(0).getDate()).isEqualTo(startDate);
    }

    @Test
    @DisplayName("Should exclude bookings after end date")
    void getDailySalesForDateRange_EndDateExclusive_ExcludesAfterEndDate() {
        LocalDate startDate = baseDateTime.toLocalDate();
        LocalDate endDate = startDate.plusDays(5);

        createAndPersistBooking(baseDateTime, 50.0, Booking.BookingStatus.CONFIRMED, 1);
        createAndPersistBooking(endDate.atTime(0, 0), 75.0, Booking.BookingStatus.CONFIRMED, 1);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);

        assertThat(sales).hasSize(1);
        assertThat(sales.get(0).getDate()).isEqualTo(startDate);
    }

    @Test
    @DisplayName("Should map RowMapper correctly to DailySalesDTO")
    void getDailySalesForDateRange_RowMapper_MapsAllFields() {
        LocalDate startDate = baseDateTime.toLocalDate();
        LocalDate endDate = startDate.plusDays(1);

        createAndPersistBooking(baseDateTime, 100.0, Booking.BookingStatus.CONFIRMED, 2);

        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);

        DailySalesDTO dto = sales.get(0);
        assertThat(dto.getDate()).isEqualTo(startDate);
        assertThat(dto.getTotalRevenue()).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(dto.getTicketsSold()).isEqualTo(2);
    }

    // ========== Helper Methods ==========

    private Booking createAndPersistBooking(LocalDateTime createdAt, double totalPrice,
                                            Booking.BookingStatus status, int seatsCount) {
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

        for (int i = 0; i < seatsCount; i++) {
            String insertSql = "INSERT INTO booking_seats (booking_id, seat_id, ticket_type_id, price, seat_status) VALUES (?, ?, ?, ?, ?)";
            double seatPrice = seatsCount > 0 ? totalPrice / seatsCount : totalPrice;
            jdbcTemplate.update(
                insertSql,
                booking.getId(),
                persistSeatId(),
                ticketType.getId(),
                seatPrice,
                "RESERVED"
            );
        }

        return booking;
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
