package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    
    private Long id;
    
    private Long hallId;
    
    private Integer rowNumber;
    
    private Integer seatNumber;
    
    private String seatType;
    
    private Boolean active;
    
    private Boolean available;
    
    private String status;
}
