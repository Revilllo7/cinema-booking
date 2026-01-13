package com.cinema.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(@NotNull Long ticketTypeId) {
}
