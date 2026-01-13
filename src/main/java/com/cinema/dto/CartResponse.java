package com.cinema.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CartResponse {
    Long screeningId;
    List<CartItemResponse> items;
    Double subtotal;
}
