package com.cinema.controller.rest;

import com.cinema.dto.MovieImageDTO;
import com.cinema.service.MovieImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movie Images", description = "Manage additional gallery images for movies")
public class MovieImageRestController {

    private final MovieImageService movieImageService;

    @Operation(summary = "List gallery images for a movie")
    @GetMapping
    public ResponseEntity<List<MovieImageDTO>> getGallery(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieImageService.getGallery(movieId));
    }

    @Operation(summary = "Upload gallery images", description = "Admin-only endpoint for uploading one or more images via multipart form data")
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MovieImageDTO>> uploadImages(@PathVariable Long movieId,
                                                            @RequestParam("files") List<MultipartFile> files,
                                                            @RequestParam(value = "captions", required = false) List<String> captions) {
        log.info("Uploading {} image(s) for movie {}", files != null ? files.size() : 0, movieId);
        List<MovieImageDTO> gallery = movieImageService.addImages(movieId, files, captions);
        return ResponseEntity.status(HttpStatus.CREATED).body(gallery);
    }

    @Operation(summary = "Delete a gallery image", description = "Requires at least 3 images to remain for the movie")
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long movieId, @PathVariable Long imageId) {
        movieImageService.deleteImage(movieId, imageId);
        return ResponseEntity.noContent().build();
    }
}
