package com.cinema.service;

import com.cinema.dto.DailySalesDTO;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class StatsCsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public byte[] exportDailySales(List<DailySalesDTO> sales) {
        StringBuilder builder = new StringBuilder();
        builder.append("Date,Tickets Sold,Revenue\n");

        for (DailySalesDTO dto : sales) {
            builder.append(formatDate(dto))
                   .append(',')
                   .append(formatTickets(dto))
                   .append(',')
                   .append(formatRevenue(dto))
                   .append('\n');
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatDate(DailySalesDTO dto) {
        return dto.getDate() != null ? DATE_FORMATTER.format(dto.getDate()) : "";
    }

    private long formatTickets(DailySalesDTO dto) {
        return dto.getTicketsSold() != null ? dto.getTicketsSold() : 0L;
    }

    private String formatRevenue(DailySalesDTO dto) {
        double revenue = dto.getTotalRevenue() != null ? dto.getTotalRevenue() : 0.0;
        return String.format(Locale.US, "%.2f", revenue);
    }
}
