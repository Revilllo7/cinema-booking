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
public class AdminBookingDTO {

    private Long id;
    private String bookingNumber;
    private LocalDateTime createdAt;
    private Booking.BookingStatus status;
    private Double totalPrice;
    private String paymentMethod;
    private String customerEmail;
    private String customerPhone;

    private Long userId;
    private String username;
    private String userEmail;

    private Long movieId;
    private String movieTitle;

    private Long screeningId;
    private LocalDateTime screeningStartTime;
    private LocalDateTime screeningEndTime;
    private String hallName;

    private Long seatsCount;
}
