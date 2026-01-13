package com.cinema.cart;

import com.cinema.entity.TicketTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private Long seatId;
    private Integer rowNumber;
    private Integer seatNumber;
    private TicketTypeName ticketType;
    private Long ticketTypeId;
    private Double price;
    private LocalDateTime lockExpiresAt;
}
