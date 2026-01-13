package com.cinema.controller.rest;

import com.cinema.dto.CheckoutRequest;
import com.cinema.dto.CheckoutResponse;
import com.cinema.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/screenings/{screeningId}/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Finalize ticket purchase for a screening")
public class CheckoutRestController {

    private final CheckoutService checkoutService;

    @Operation(summary = "Complete checkout", description = "Captures payment details, confirms the booking, and returns a QR code")
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable Long screeningId,
                                                     @Valid @RequestBody CheckoutRequest request,
                                                     HttpServletRequest servletRequest,
                                                     Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required to checkout");
        }
        String sessionId = servletRequest.getSession(true).getId();
        CheckoutResponse response = checkoutService.finalizeCheckout(screeningId, request, sessionId, principal.getName());
        return ResponseEntity.ok(response);
    }
}
