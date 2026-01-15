package com.cinema.controller.rest;

import com.cinema.dto.DailySalesDTO;
import com.cinema.repository.jdbc.BookingJdbcRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Statistics", description = "Admin endpoints for sales and booking statistics")
@SecurityRequirement(name = "bearerAuth")
public class AdminStatsRestController {

    private final BookingJdbcRepository bookingJdbcRepository;

    @GetMapping("/sales")
    @Operation(summary = "Get daily sales statistics", 
               description = "Returns daily sales data for the current month or specified month/date range")
    public ResponseEntity<List<DailySalesDTO>> getDailySales(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        List<DailySalesDTO> sales;
        
        if (startDate != null && endDate != null) {
            sales = bookingJdbcRepository.getDailySalesForDateRange(startDate, endDate);
        } else {
            // Default to current month
            YearMonth currentMonth = YearMonth.now();
            int targetYear = year != null ? year : currentMonth.getYear();
            int targetMonth = month != null ? month : currentMonth.getMonthValue();
            
            sales = bookingJdbcRepository.getDailySalesForMonth(targetYear, targetMonth);
        }
        
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/sales/current-month")
    @Operation(summary = "Get current month sales", 
               description = "Returns daily sales data for the current month")
    public ResponseEntity<List<DailySalesDTO>> getCurrentMonthSales() {
        YearMonth currentMonth = YearMonth.now();
        List<DailySalesDTO> sales = bookingJdbcRepository.getDailySalesForMonth(
            currentMonth.getYear(), 
            currentMonth.getMonthValue()
        );
        return ResponseEntity.ok(sales);
    }
}
