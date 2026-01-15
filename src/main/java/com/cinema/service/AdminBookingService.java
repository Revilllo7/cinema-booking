package com.cinema.service;

import com.cinema.dto.AdminBookingDTO;
import com.cinema.dto.AdminBookingFilter;
import com.cinema.dto.AdminBookingSummaryDTO;
import com.cinema.entity.Booking;
import com.cinema.repository.AdminBookingRepository;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.jdbc.ReportingDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookingService {

    private final AdminBookingRepository adminBookingRepository;
    private final BookingRepository bookingRepository;
    private final ReportingDao reportingDao;

    @Transactional(readOnly = true)
    public Page<AdminBookingDTO> getAdminBookings(AdminBookingFilter filter, Pageable pageable) {
        log.debug("Fetching admin bookings with filter {} and pageable {}", filter, pageable);
        return adminBookingRepository.findBookings(filter, pageable);
    }

    @Transactional(readOnly = true)
    public AdminBookingSummaryDTO getSummary() {
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);
        long pendingBookings = bookingRepository.countByStatus(Booking.BookingStatus.PENDING);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().minusSeconds(1);

        BigDecimal revenue = reportingDao.totalRevenueBetween(monthStart, monthEnd);
        int tickets = reportingDao.ticketsSoldBetween(monthStart, monthEnd);

        return AdminBookingSummaryDTO.builder()
            .totalBookings(totalBookings)
            .confirmedBookings(confirmedBookings)
            .pendingBookings(pendingBookings)
            .currentMonthRevenue(revenue.doubleValue())
            .currentMonthTickets(tickets)
            .build();
    }
}
