package com.cinema.dto;

import com.cinema.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingFilter {

    private String search;
    private Booking.BookingStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
