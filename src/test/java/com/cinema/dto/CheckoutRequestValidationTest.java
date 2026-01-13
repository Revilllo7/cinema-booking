package com.cinema.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void validRequest_PassesValidation() {
        CheckoutRequest request = new CheckoutRequest(
            "valid@example.com",
            "+48123456789",
            "Jane Customer",
            "CARD"
        );

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void missingRequiredFields_FailsValidation() {
        CheckoutRequest request = new CheckoutRequest(
            "",
            "123",
            "",
            ""
        );

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertThat(violations)
            .hasSizeGreaterThanOrEqualTo(3)
            .anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail"))
            .anyMatch(v -> v.getPropertyPath().toString().equals("cardholderName"))
            .anyMatch(v -> v.getPropertyPath().toString().equals("paymentMethod"));
    }

    @Test
    void invalidPhoneFormat_FailsValidation() {
        CheckoutRequest request = new CheckoutRequest(
            "valid@example.com",
            "ABC",
            "Jane Customer",
            "CARD"
        );

        Set<ConstraintViolation<CheckoutRequest>> violations = validator.validate(request);

        assertThat(violations)
            .anyMatch(v -> v.getPropertyPath().toString().equals("customerPhone"));
    }
}
