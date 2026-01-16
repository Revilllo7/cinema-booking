package com.cinema.service;

import com.cinema.dto.AdminBookingDTO;
import com.cinema.dto.AdminBookingFilter;
import com.cinema.dto.AdminBookingSummaryDTO;
import com.cinema.entity.Booking;
import com.cinema.repository.AdminBookingRepository;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.jdbc.ReportingDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminBookingService using Mockito.
 * Tests admin booking management and reporting functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBookingService Tests")
class AdminBookingServiceTest {

    @Mock
    private AdminBookingRepository adminBookingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReportingDao reportingDao;

    @InjectMocks
    private AdminBookingService adminBookingService;

    private AdminBookingFilter filter;

    @BeforeEach
    void setUp() {
        filter = AdminBookingFilter.builder().build();
    }

    @Nested
    @DisplayName("Get Admin Bookings Tests")
    class GetAdminBookingsTests {

        @Test
        @DisplayName("Should return paginated bookings with filter")
        void getAdminBookings_WithFilter_ReturnsPaginatedResults() {
            AdminBookingDTO dto = AdminBookingDTO.builder()
                .id(1L)
                .bookingNumber("BK-001")
                .status(Booking.BookingStatus.CONFIRMED)
                .totalPrice(100.0)
                .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<AdminBookingDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

            when(adminBookingRepository.findBookings(filter, pageable)).thenReturn(page);

            Page<AdminBookingDTO> result = adminBookingService.getAdminBookings(filter, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getBookingNumber()).isEqualTo("BK-001");

            verify(adminBookingRepository).findBookings(filter, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no bookings match filter")
        void getAdminBookings_NoMatches_ReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AdminBookingDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(adminBookingRepository.findBookings(filter, pageable)).thenReturn(emptyPage);

            Page<AdminBookingDTO> result = adminBookingService.getAdminBookings(filter, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Should support pagination")
        void getAdminBookings_WithPagination_SupportsDifferentPages() {
            AdminBookingDTO dto1 = AdminBookingDTO.builder().id(1L).bookingNumber("BK-001").build();
            AdminBookingDTO dto2 = AdminBookingDTO.builder().id(2L).bookingNumber("BK-002").build();

            Pageable page1 = PageRequest.of(0, 1);
            Pageable page2 = PageRequest.of(1, 1);

            when(adminBookingRepository.findBookings(filter, page1))
                .thenReturn(new PageImpl<>(List.of(dto1), page1, 2));
            when(adminBookingRepository.findBookings(filter, page2))
                .thenReturn(new PageImpl<>(List.of(dto2), page2, 2));

            Page<AdminBookingDTO> result1 = adminBookingService.getAdminBookings(filter, page1);
            Page<AdminBookingDTO> result2 = adminBookingService.getAdminBookings(filter, page2);

            assertThat(result1.getContent().get(0).getBookingNumber()).isEqualTo("BK-001");
            assertThat(result2.getContent().get(0).getBookingNumber()).isEqualTo("BK-002");
        }

        @Test
        @DisplayName("Should apply status filter")
        void getAdminBookings_WithStatusFilter_FiltersByStatus() {
            filter.setStatus(Booking.BookingStatus.CONFIRMED);
            AdminBookingDTO confirmedDto = AdminBookingDTO.builder()
                .id(1L)
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<AdminBookingDTO> page = new PageImpl<>(List.of(confirmedDto), pageable, 1);

            when(adminBookingRepository.findBookings(filter, pageable)).thenReturn(page);

            Page<AdminBookingDTO> result = adminBookingService.getAdminBookings(filter, pageable);

            assertThat(result.getContent()).allMatch(dto -> Booking.BookingStatus.CONFIRMED.equals(dto.getStatus()));
        }
    }

    @Nested
    @DisplayName("Get Summary Tests")
    class GetSummaryTests {

        @Test
        @DisplayName("Should calculate summary with correct statistics")
        void getSummary_CalculatesAllMetrics() {
            long totalBookings = 100;
            long confirmedBookings = 80;
            long pendingBookings = 20;
            BigDecimal revenue = new BigDecimal("5000.00");
            int tickets = 150;

            when(bookingRepository.count()).thenReturn(totalBookings);
            when(bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED))
                .thenReturn(confirmedBookings);
            when(bookingRepository.countByStatus(Booking.BookingStatus.PENDING))
                .thenReturn(pendingBookings);
            when(reportingDao.totalRevenueBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(revenue);
            when(reportingDao.ticketsSoldBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);

            AdminBookingSummaryDTO summary = adminBookingService.getSummary();

            assertThat(summary.getTotalBookings()).isEqualTo(totalBookings);
            assertThat(summary.getConfirmedBookings()).isEqualTo(confirmedBookings);
            assertThat(summary.getPendingBookings()).isEqualTo(pendingBookings);
            assertThat(summary.getCurrentMonthRevenue()).isEqualTo(5000.00);
            assertThat(summary.getCurrentMonthTickets()).isEqualTo(tickets);
        }

        @Test
        @DisplayName("Should handle zero bookings")
        void getSummary_WithZeroBookings_ReturnsZeroTotals() {
            when(bookingRepository.count()).thenReturn(0L);
            when(bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED)).thenReturn(0L);
            when(bookingRepository.countByStatus(Booking.BookingStatus.PENDING)).thenReturn(0L);
            when(reportingDao.totalRevenueBetween(any(), any())).thenReturn(BigDecimal.ZERO);
            when(reportingDao.ticketsSoldBetween(any(), any())).thenReturn(0);

            AdminBookingSummaryDTO summary = adminBookingService.getSummary();

            assertThat(summary.getTotalBookings()).isZero();
            assertThat(summary.getConfirmedBookings()).isZero();
            assertThat(summary.getPendingBookings()).isZero();
            assertThat(summary.getCurrentMonthRevenue()).isZero();
            assertThat(summary.getCurrentMonthTickets()).isZero();
        }

        @Test
        @DisplayName("Should query current month range")
        void getSummary_QuerriesCurrentMonth_TimeBounds() {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().minusSeconds(1);

            when(bookingRepository.count()).thenReturn(0L);
            when(bookingRepository.countByStatus(any())).thenReturn(0L);
            when(reportingDao.totalRevenueBetween(any(), any())).thenReturn(BigDecimal.ZERO);
            when(reportingDao.ticketsSoldBetween(any(), any())).thenReturn(0);

            adminBookingService.getSummary();

            verify(reportingDao, times(1)).totalRevenueBetween(eq(monthStart), eq(monthEnd));
            verify(reportingDao, times(1)).ticketsSoldBetween(eq(monthStart), eq(monthEnd));
        }

        @Test
        @DisplayName("Should correctly calculate revenue for month")
        void getSummary_CalculatesMonthlyRevenue_Correctly() {
            BigDecimal monthlyRevenue = new BigDecimal("2500.50");

            when(bookingRepository.count()).thenReturn(50L);
            when(bookingRepository.countByStatus(any())).thenReturn(25L);
            when(reportingDao.totalRevenueBetween(any(), any())).thenReturn(monthlyRevenue);
            when(reportingDao.ticketsSoldBetween(any(), any())).thenReturn(100);

            AdminBookingSummaryDTO summary = adminBookingService.getSummary();

            assertThat(summary.getCurrentMonthRevenue()).isCloseTo(2500.50, org.assertj.core.data.Offset.offset(0.01));
        }
    }

    @Nested
    @DisplayName("Filter Combination Tests")
    class FilterCombinationTests {

        @Test
        @DisplayName("Should support multiple filters together")
        void getAdminBookings_MultipleFilters_AppliesAll() {
            filter.setStatus(Booking.BookingStatus.CONFIRMED);

            Pageable pageable = PageRequest.of(0, 10);
            Page<AdminBookingDTO> page = new PageImpl<>(List.of(), pageable, 0);

            when(adminBookingRepository.findBookings(filter, pageable)).thenReturn(page);

            adminBookingService.getAdminBookings(filter, pageable);

            verify(adminBookingRepository).findBookings(filter, pageable);
        }

        @Test
        @DisplayName("Should handle empty filter")
        void getAdminBookings_EmptyFilter_ReturnsAllBookings() {
            AdminBookingFilter emptyFilter = AdminBookingFilter.builder().build();
            AdminBookingDTO dto = AdminBookingDTO.builder().id(1L).build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<AdminBookingDTO> page = new PageImpl<>(List.of(dto), pageable, 1);

            when(adminBookingRepository.findBookings(emptyFilter, pageable)).thenReturn(page);

            Page<AdminBookingDTO> result = adminBookingService.getAdminBookings(emptyFilter, pageable);

            assertThat(result.getContent()).isNotEmpty();
        }
    }
}
