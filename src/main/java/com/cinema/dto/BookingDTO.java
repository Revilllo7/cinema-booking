package com.cinema.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    
    private Long id;
    
    private String bookingNumber;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Screening ID is required")
    private Long screeningId;
    
    @NotEmpty(message = "At least one seat must be selected")
    @Valid
    private List<BookingSeatRequest> seats;
    
    private Double totalPrice;
    
    private String status;
    
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Customer email is required")
    private String customerEmail;
    
    @Pattern(regexp = "^\\+?[0-9]{9,20}$", message = "Invalid phone number format")
    private String customerPhone;
    
    private LocalDateTime createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingSeatRequest {
        
        @NotNull(message = "Seat ID is required")
        private Long seatId;
        
        @NotNull(message = "Ticket type ID is required")
        private Long ticketTypeId;
    }
}
