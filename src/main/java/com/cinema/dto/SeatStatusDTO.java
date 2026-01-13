package com.cinema.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class SeatStatusDTO {
    Long seatId;
    int rowNumber;
    int seatNumber;
    SeatState status;
    boolean selectedByYou;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime lockExpiresAt;

    public enum SeatState {
        FREE,
        BOOKED,
        SOLD
    }
}
