package com.cinema.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CheckoutResponse {
    String bookingNumber;
    Double totalPrice;
    String paymentReference;
    List<CartItemResponse> items;
    String qrCodeImage;
}
