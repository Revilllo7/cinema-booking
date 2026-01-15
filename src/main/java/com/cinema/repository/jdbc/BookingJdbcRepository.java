package com.cinema.repository.jdbc;

import com.cinema.dto.DailySalesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<DailySalesDTO> getDailySalesForMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endExclusive = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        String sql = baseRangeQuery();
        return jdbcTemplate.query(sql, new DailySalesRowMapper(),
            Timestamp.valueOf(start), Timestamp.valueOf(endExclusive));
    }

    public List<DailySalesDTO> getDailySalesForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        String sql = baseRangeQuery();
        return jdbcTemplate.query(sql, new DailySalesRowMapper(),
            Timestamp.valueOf(start), Timestamp.valueOf(endExclusive));
    }

    private String baseRangeQuery() {
        return """
            SELECT 
                DATE(b.created_at) as booking_date,
                COALESCE(SUM(b.total_price), 0) as total_revenue,
                COALESCE(COUNT(bs.id), 0) as tickets_sold
            FROM bookings b
            LEFT JOIN booking_seats bs ON b.id = bs.booking_id
            WHERE b.created_at >= ?
              AND b.created_at < ?
              AND b.status IN ('CONFIRMED', 'COMPLETED', 'PENDING')
            GROUP BY DATE(b.created_at)
            ORDER BY booking_date
            """;
    }

    private static class DailySalesRowMapper implements RowMapper<DailySalesDTO> {
        @Override
        public DailySalesDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            DailySalesDTO dto = new DailySalesDTO();
            dto.setDate(rs.getDate("booking_date").toLocalDate());
            dto.setTotalRevenue(rs.getDouble("total_revenue"));
            dto.setTicketsSold(rs.getLong("tickets_sold"));
            return dto;
        }
    }
}
