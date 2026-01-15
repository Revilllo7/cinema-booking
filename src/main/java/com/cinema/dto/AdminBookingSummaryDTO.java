package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingSummaryDTO {

    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private double currentMonthRevenue;
    private long currentMonthTickets;
}
