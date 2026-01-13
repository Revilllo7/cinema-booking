package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer rowsCount;
    private Integer seatsPerRow;
    private String description;
}
