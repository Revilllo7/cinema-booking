package com.cinema.dto;

import com.cinema.entity.TicketTypeName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TicketOptionResponse {
    Long ticketTypeId;
    TicketTypeName name;
    Double price;
}
