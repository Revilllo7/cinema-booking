package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class SeatMapResponse {
    int rows;
    int cols;
    int holdMinutes;
    List<SeatStatusDTO> seats;
}
