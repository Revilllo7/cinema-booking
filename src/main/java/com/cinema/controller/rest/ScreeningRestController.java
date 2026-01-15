package com.cinema.controller.rest;

import com.cinema.dto.ScreeningDTO;
import com.cinema.dto.SeatMapResponse;
import com.cinema.service.ScreeningService;
import com.cinema.service.SeatReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/screenings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Screenings", description = "Screening management API")
public class ScreeningRestController {

    private final ScreeningService screeningService;
    private final SeatReservationService seatReservationService;

    @Operation(summary = "Get all upcoming screenings", description = "Retrieve a paginated list of all upcoming screenings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved screenings"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<ScreeningDTO>> getAllScreenings(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "startTime") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @Parameter(description = "Filter by movie ID") @RequestParam(required = false) Long movieId,
            @Parameter(description = "Filter by start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter by end date") @RequestParam(required = false) String endDate) {
        
        log.info("GET /api/v1/screenings - page: {}, size: {}, sortBy: {}, movieId: {}, startDate: {}, endDate: {}", page, size, sortBy, movieId, startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ScreeningDTO> screenings = screeningService.getAllActiveScreenings(pageable, movieId, startDate, endDate);
        
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "Get seat map for screening", description = "Retrieve hall layout and occupied seats for a screening")
    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long id,
                                                      HttpServletRequest request,
                                                      Principal principal) {
        String sessionId = request.getSession(true).getId();
        String username = principal != null ? principal.getName() : null;
        SeatMapResponse seatMap = seatReservationService.getSeatMap(id, sessionId, username);
        return ResponseEntity.ok(seatMap);
    }

    @Operation(summary = "Get screening by ID", description = "Retrieve a single screening by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved screening"),
        @ApiResponse(responseCode = "404", description = "Screening not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDTO> getScreeningById(
            @Parameter(description = "Screening ID") @PathVariable Long id) {
        
        log.info("GET /api/v1/screenings/{}", id);
        
        ScreeningDTO screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }

    @Operation(summary = "Get screenings by movie", description = "Retrieve all upcoming screenings for a specific movie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved screenings"),
        @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByMovie(
            @Parameter(description = "Movie ID") @PathVariable Long movieId) {
        
        log.info("GET /api/v1/screenings/movie/{}", movieId);
        
        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(movieId);
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "Get screenings by hall", description = "Retrieve all upcoming screenings for a hall")
    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByHall(
            @Parameter(description = "Hall ID") @PathVariable Long hallId) {

        log.info("GET /api/v1/screenings/hall/{}", hallId);

        List<ScreeningDTO> screenings = screeningService.getScreeningsByHall(hallId);
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "Get screenings by hall and date range", description = "Retrieve all screenings for a hall within a date range")
    @GetMapping(value = "/hall/{hallId}", params = {"startDate", "endDate"})
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByHallAndDateRange(
            @Parameter(description = "Hall ID") @PathVariable Long hallId,
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("GET /api/v1/screenings/hall/{}?startDate={}&endDate={}", hallId, startDate, endDate);

        List<ScreeningDTO> screenings = screeningService.getScreeningsByHallAndDateRange(hallId, startDate, endDate);
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "Get screenings by hall and date range", description = "Retrieve all screenings for a hall within a date range")
    @GetMapping(value = "/hall/{hallId}/date-range")
    public ResponseEntity<List<ScreeningDTO>> getScreeningsByHallAndDateRangePath(
            @Parameter(description = "Hall ID") @PathVariable Long hallId,
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("GET /api/v1/screenings/hall/{}/date-range?startDate={}&endDate={}", hallId, startDate, endDate);

        List<ScreeningDTO> screenings = screeningService.getScreeningsByHallAndDateRange(hallId, startDate, endDate);
        return ResponseEntity.ok(screenings);
    }

    @Operation(summary = "Create new screening", description = "Create a new screening (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Screening created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or screening conflict"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Movie or Hall not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> createScreening(@Valid @RequestBody ScreeningDTO screeningDTO) {
        log.info("POST /api/v1/screenings - Creating screening for movie id: {}, hall id: {}", 
            screeningDTO.getMovieId(), screeningDTO.getHallId());
        
        ScreeningDTO createdScreening = screeningService.createScreening(screeningDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdScreening);
    }

    @Operation(summary = "Update screening", description = "Update an existing screening (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Screening updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or screening conflict"),
        @ApiResponse(responseCode = "404", description = "Screening not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScreeningDTO> updateScreening(
            @Parameter(description = "Screening ID") @PathVariable Long id,
            @Valid @RequestBody ScreeningDTO screeningDTO) {
        
        log.info("PUT /api/v1/screenings/{} - Updating screening", id);
        
        ScreeningDTO updatedScreening = screeningService.updateScreening(id, screeningDTO);
        return ResponseEntity.ok(updatedScreening);
    }

    @Operation(summary = "Delete screening", description = "Deactivate a screening (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Screening deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Screening not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScreening(@Parameter(description = "Screening ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/screenings/{}", id);
        
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }
}
