package com.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    String customerEmail,

    @Pattern(regexp = "^$|^\\+?[0-9]{9,20}$", message = "Phone must contain 9-20 digits")
    String customerPhone,

    @NotBlank(message = "Cardholder name is required")
    @Size(max = 100, message = "Cardholder name is too long")
    String cardholderName,

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method is too long")
    String paymentMethod
) {
}
