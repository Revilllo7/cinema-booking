package com.cinema.controller.rest;

import com.cinema.dto.SeatStatusDTO;
import com.cinema.service.SeatReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/screenings/{screeningId}/locks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seat Locks", description = "Lock and release seats during selection")
public class SeatReservationRestController {

    private final SeatReservationService seatReservationService;

    @Operation(summary = "Lock a seat for the current session", description = "Creates a 10-minute hold for the specified seat")
    @PostMapping
    public ResponseEntity<SeatStatusDTO> lockSeat(@PathVariable Long screeningId,
                                                  @RequestBody SeatLockRequest request,
                                                  HttpServletRequest servletRequest,
                                                  Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        SeatStatusDTO seatStatus = seatReservationService.lockSeat(screeningId, request.seatId(), sessionId, username);
        return ResponseEntity.ok(seatStatus);
    }

    @Operation(summary = "Release a seat lock for the current session")
    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> releaseSeat(@PathVariable Long screeningId,
                                            @PathVariable Long seatId,
                                            HttpServletRequest servletRequest,
                                            Principal principal) {
        String sessionId = servletRequest.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        seatReservationService.releaseSeat(screeningId, seatId, sessionId, username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Release all locks held by this session")
    @DeleteMapping
    public ResponseEntity<Void> releaseAll(@PathVariable Long screeningId,
                                           HttpServletRequest servletRequest) {
        String sessionId = servletRequest.getSession(true).getId();
        seatReservationService.releaseAll(screeningId, sessionId);
        return ResponseEntity.noContent().build();
    }

    public record SeatLockRequest(@NotNull Long seatId) {}
}
