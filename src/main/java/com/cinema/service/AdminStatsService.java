package com.cinema.service;

import com.cinema.dto.DailySalesDTO;
import com.cinema.repository.jdbc.BookingJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final BookingJdbcRepository bookingJdbcRepository;

    public List<DailySalesDTO> getDailySalesForMonth(int year, int month) {
        return bookingJdbcRepository.getDailySalesForMonth(year, month);
    }

    public List<DailySalesDTO> getDailySalesForDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);
    }

    public List<DailySalesDTO> getCurrentMonthSales() {
        YearMonth currentMonth = YearMonth.now();
        return getDailySalesForMonth(currentMonth.getYear(), currentMonth.getMonthValue());
    }
}
