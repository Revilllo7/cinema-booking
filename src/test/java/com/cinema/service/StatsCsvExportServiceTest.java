package com.cinema.service;

import com.cinema.dto.DailySalesDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatsCsvExportServiceTest {

    private final StatsCsvExportService service = new StatsCsvExportService();

    @Test
    void exportDailySales_ReturnsHeaderAndRows() {
        List<DailySalesDTO> rows = List.of(
            new DailySalesDTO(LocalDate.of(2025, 1, 1), 120.5, 10L),
            new DailySalesDTO(LocalDate.of(2025, 1, 2), null, null)
        );

        byte[] csvBytes = service.exportDailySales(rows);
        String csv = new String(csvBytes);

        assertThat(csv).contains("Date,Tickets Sold,Revenue\n");
        assertThat(csv).contains("2025-01-01,10,120.50");
        assertThat(csv).contains("2025-01-02,0,0.00");
    }
}
