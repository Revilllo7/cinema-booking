package com.cinema.controller.rest;

import com.cinema.dto.AdminBookingDTO;
import com.cinema.dto.AdminBookingFilter;
import com.cinema.dto.AdminBookingSummaryDTO;
import com.cinema.entity.Booking;
import com.cinema.service.AdminBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Bookings", description = "Admin endpoints for booking management and overview")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingRestController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    @Operation(summary = "Get paginated bookings for admin",
        description = "Supports filtering by status, date range, and search across customer, booking number, and movie title")
    public ResponseEntity<Page<AdminBookingDTO>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) Booking.BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        AdminBookingFilter filter = AdminBookingFilter.builder()
            .status(status)
            .search(StringUtils.hasText(search) ? search : null)
            .startDate(startDate != null ? startDate.atStartOfDay() : null)
            .endDate(endDate != null ? endDate.atTime(LocalTime.MAX) : null)
            .build();

        log.debug("Fetching admin bookings with filter {}", filter);
        Page<AdminBookingDTO> bookings = adminBookingService.getAdminBookings(filter, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get admin booking summary",
        description = "Returns total bookings, status counts, and current month revenue/tickets")
    public ResponseEntity<AdminBookingSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminBookingService.getSummary());
    }
}
