package com.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    private String genre;
    
    @Size(max = 10, message = "Age rating must not exceed 10 characters")
    private String ageRating;
    
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 500, message = "Duration must not exceed 500 minutes")
    private Integer durationMinutes;
    
    @Size(max = 150, message = "Director name must not exceed 150 characters")
    private String director;
    
    private String cast;
    
    @Min(value = 1888, message = "Release year must be at least 1888")
    @Max(value = 2100, message = "Release year must not exceed 2100")
    private Integer releaseYear;
    
    private String posterPath;
    
    private String trailerUrl;
    
    private Boolean active;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Set<String> imagePaths;
}
