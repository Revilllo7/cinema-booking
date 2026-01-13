package com.cinema.dto;

import com.cinema.entity.TicketTypeName;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CartItemResponse {
    Long seatId;
    Integer rowNumber;
    Integer seatNumber;
    TicketTypeName ticketType;
    Long ticketTypeId;
    Double price;
    LocalDateTime lockExpiresAt;
}
