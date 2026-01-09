package com.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningDTO {
    
    private Long id;
    
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    private String movieTitle;
    
    @NotNull(message = "Hall ID is required")
    private Long hallId;
    
    private String hallName;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "1000.0", message = "Price must not exceed 1000")
    private Double basePrice;
    
    private Boolean active;
    
    private Integer availableSeats;
    
    private LocalDateTime createdAt;
}
