package com.cinema.controller.rest;

import com.cinema.dto.DailySalesDTO;
import com.cinema.service.AdminStatsService;
import com.cinema.service.StatsCsvExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    private final AdminStatsService adminStatsService;
    private final StatsCsvExportService statsCsvExportService;

    @GetMapping("/sales")
    @Operation(summary = "Get daily sales statistics", 
               description = "Returns daily sales data for the current month or specified month/date range")
    public ResponseEntity<List<DailySalesDTO>> getDailySales(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        return ResponseEntity.ok(loadSalesData(year, month, startDate, endDate));
    }

    @GetMapping("/sales/current-month")
    @Operation(summary = "Get current month sales", 
               description = "Returns daily sales data for the current month")
    public ResponseEntity<List<DailySalesDTO>> getCurrentMonthSales() {
        return ResponseEntity.ok(adminStatsService.getCurrentMonthSales());
    }

    @GetMapping(value = "/sales/csv", produces = "text/csv")
    @Operation(summary = "Download daily sales CSV",
               description = "Returns a CSV file with the same data as the daily sales endpoint")
    public ResponseEntity<byte[]> downloadDailySalesCsv(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        List<DailySalesDTO> sales = loadSalesData(year, month, startDate, endDate);
        byte[] csv = statsCsvExportService.exportDailySales(sales);
        String filename = buildFilename(year, month, startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private List<DailySalesDTO> loadSalesData(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return adminStatsService.getDailySalesForDateRange(startDate, endDate);
        }

        YearMonth targetMonth = resolveTargetMonth(year, month);
        return adminStatsService.getDailySalesForMonth(targetMonth.getYear(), targetMonth.getMonthValue());
    }

    private YearMonth resolveTargetMonth(Integer year, Integer month) {
        YearMonth current = YearMonth.now();
        int resolvedYear = year != null ? year : current.getYear();
        int resolvedMonth = month != null ? month : current.getMonthValue();
        return YearMonth.of(resolvedYear, resolvedMonth);
    }

    private String buildFilename(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return String.format("daily-sales-%s-to-%s.csv", startDate, endDate);
        }

        YearMonth targetMonth = resolveTargetMonth(year, month);
        return String.format("daily-sales-%d-%02d.csv", targetMonth.getYear(), targetMonth.getMonthValue());
    }
}
