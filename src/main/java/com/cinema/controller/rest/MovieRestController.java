package com.cinema.controller.rest;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movies", description = "Movie management API")
public class MovieRestController {

    private final MovieService movieService;

    @Operation(summary = "Get all active movies", description = "Retrieve a paginated list of all active movies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved movies"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<MovieDTO>> getAllMovies(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @Parameter(description = "Search by title") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by genre") @RequestParam(required = false) String genre) {
        
        log.info("GET /api/v1/movies - page: {}, size: {}, sortBy: {}, search: {}, genre: {}", page, size, sortBy, search, genre);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<MovieDTO> movies = movieService.getAllActiveMovies(pageable, search, genre);
        
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Get movie by ID", description = "Retrieve a single movie by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved movie"),
        @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(
            @Parameter(description = "Movie ID") @PathVariable Long id) {
        
        log.info("GET /api/v1/movies/{}", id);
        
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @Operation(summary = "Search movies by title", description = "Search for movies by title keyword")
    @GetMapping("/search")
    public ResponseEntity<Page<MovieDTO>> searchMovies(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/v1/movies/search?keyword={}", keyword);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieDTO> movies = movieService.searchMoviesByTitle(keyword, pageable);
        
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Get movies by genre", description = "Retrieve movies filtered by genre")
    @GetMapping("/genre/{genre}")
    public ResponseEntity<Page<MovieDTO>> getMoviesByGenre(
            @Parameter(description = "Genre name") @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/v1/movies/genre/{}", genre);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieDTO> movies = movieService.getMoviesByGenre(genre, pageable);
        
        return ResponseEntity.ok(movies);
    }

    @Operation(summary = "Get all genres", description = "Retrieve list of all available genres")
    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        log.info("GET /api/v1/movies/genres");
        
        List<String> genres = movieService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "Create new movie", description = "Create a new movie (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movie created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> createMovie(@Valid @RequestBody MovieDTO movieDto) {
        log.info("POST /api/v1/movies - Creating movie: {}", movieDto.getTitle());
        
        MovieDTO createdMovie = movieService.createMovie(movieDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
    }

    @Operation(summary = "Update movie", description = "Update an existing movie (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Movie not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> updateMovie(
            @Parameter(description = "Movie ID") @PathVariable Long id,
            @Valid @RequestBody MovieDTO movieDto) {
        
        log.info("PUT /api/v1/movies/{} - Updating movie", id);
        
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDto);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "Delete movie", description = "Deactivate a movie (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Movie not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@Parameter(description = "Movie ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/movies/{}", id);
        
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload/replace poster", description = "Upload a new primary poster for a movie (Admin only)")
    @PostMapping(value = "/{id}/poster", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTO> uploadPoster(
            @Parameter(description = "Movie ID") @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /api/v1/movies/{}/poster - uploading poster", id);
        MovieDTO updated = movieService.updatePoster(id, file);
        return ResponseEntity.ok(updated);
    }
}
