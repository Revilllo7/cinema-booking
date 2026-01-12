package com.cinema.repository.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportingDao {
    private final JdbcTemplate jdbcTemplate;

    public ReportingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Total revenue (sum of bookings.total_price) for CONFIRMED bookings
     * created within the given time range.
     */
    public BigDecimal totalRevenueBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM bookings " +
                     "WHERE status = 'CONFIRMED' AND created_at BETWEEN ? AND ?";
        BigDecimal result = jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Tickets sold (count of booking_seats) for CONFIRMED bookings in a time range.
     */
    public int ticketsSoldBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(COUNT(bs.id), 0) FROM booking_seats bs " +
                     "JOIN bookings b ON b.id = bs.booking_id " +
                     "WHERE b.status = 'CONFIRMED' AND b.created_at BETWEEN ? AND ?";
        Integer result = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
        return result != null ? result : 0;
    }

    /**
     * Revenue by movie (sum of bookings.total_price for screenings of the movie).
     */
    public BigDecimal revenueByMovie(Long movieId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(b.total_price), 0) FROM bookings b " +
                     "JOIN screenings s ON s.id = b.screening_id " +
                     "WHERE b.status = 'CONFIRMED' AND s.movie_id = ? AND b.created_at BETWEEN ? AND ?";
        BigDecimal result = jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                movieId,
                Timestamp.valueOf(start),
                Timestamp.valueOf(end)
        );
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Seats booked for a specific screening (count of booking_seats).
     */
    public int seatsBookedForScreening(Long screeningId) {
        String sql = "SELECT COALESCE(COUNT(bs.id), 0) FROM booking_seats bs " +
                     "JOIN bookings b ON b.id = bs.booking_id " +
                     "WHERE b.screening_id = ? AND b.status = 'CONFIRMED'";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, screeningId);
        return result != null ? result : 0;
    }
}
