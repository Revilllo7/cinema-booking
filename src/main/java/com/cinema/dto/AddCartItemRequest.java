package com.cinema.dto;

import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(@NotNull Long seatId,
                                 @NotNull Long ticketTypeId) {
}
