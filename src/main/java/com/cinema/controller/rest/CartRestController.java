package com.cinema.controller.rest;

import com.cinema.dto.AddCartItemRequest;
import com.cinema.dto.CartResponse;
import com.cinema.dto.TicketOptionResponse;
import com.cinema.dto.UpdateCartItemRequest;
import com.cinema.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/screenings/{screeningId}/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Manage the session-based ticket cart")
public class CartRestController {

    private final CartService cartService;

    @Operation(summary = "Get the current cart for this screening")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@PathVariable Long screeningId,
                                                HttpServletRequest servletRequest,
                                                Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(cartService.getCart(screeningId, sessionId, username));
    }

    @Operation(summary = "List active ticket type options")
    @GetMapping("/ticket-options")
    public ResponseEntity<List<TicketOptionResponse>> getTicketOptions() {
        return ResponseEntity.ok(cartService.getTicketOptions());
    }

    @Operation(summary = "Add a locked seat to the cart")
    @PostMapping
    public ResponseEntity<CartResponse> addSeat(@PathVariable Long screeningId,
                                                @Valid @RequestBody AddCartItemRequest request,
                                                HttpServletRequest servletRequest,
                                                Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        CartResponse response = cartService.addSeat(screeningId, request.seatId(), request.ticketTypeId(), sessionId, username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update the ticket type for a seat in the cart")
    @PatchMapping("/{seatId}")
    public ResponseEntity<CartResponse> updateSeat(@PathVariable Long screeningId,
                                                   @PathVariable Long seatId,
                                                   @Valid @RequestBody UpdateCartItemRequest request,
                                                   HttpServletRequest servletRequest,
                                                   Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        CartResponse response = cartService.updateTicketType(screeningId, seatId, request.ticketTypeId(), sessionId, username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a seat from the cart and release its lock")
    @DeleteMapping("/{seatId}")
    public ResponseEntity<CartResponse> removeSeat(@PathVariable Long screeningId,
                                                   @PathVariable Long seatId,
                                                   HttpServletRequest servletRequest,
                                                   Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        CartResponse response = cartService.removeSeat(screeningId, seatId, sessionId, username);
        return ResponseEntity.ok(response);
    }
}
