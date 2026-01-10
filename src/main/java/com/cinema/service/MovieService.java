package com.cinema.service;

import com.cinema.dto.MovieDTO;
import com.cinema.entity.Movie;
import com.cinema.entity.MovieImage;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.MovieRepository;
// import com.cinema.repository.MovieImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    // private final MovieImageRepository movieImageRepository;

    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllActiveMovies(Pageable pageable) {
        log.debug("Fetching all active movies with pagination: {}", pageable);
        return movieRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public MovieDTO getMovieById(Long id) {
        log.debug("Fetching movie by id: {}", id);
        Movie movie = movieRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return convertToDto(movie);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> searchMoviesByTitle(String keyword, Pageable pageable) {
        log.debug("Searching movies by keyword: {}", keyword);
        return movieRepository.searchByTitle(keyword, pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getMoviesByGenre(String genre, Pageable pageable) {
        log.debug("Fetching movies by genre: {}", genre);
        return movieRepository.findByGenre(genre, pageable)
            .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        log.debug("Fetching all distinct genres");
        return movieRepository.findAllGenres();
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDto) {
        log.info("Creating new movie: {}", movieDto.getTitle());
        
        Movie movie = convertToEntity(movieDto);
        movie.setActive(true);
        
        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie created successfully with id: {}", savedMovie.getId());
        
        return convertToDto(savedMovie);
    }

    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO movieDto) {
        log.info("Updating movie with id: {}", id);
        
        Movie existingMovie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        updateEntityFromDto(existingMovie, movieDto);
        
        Movie updatedMovie = movieRepository.save(existingMovie);
        log.info("Movie updated successfully: {}", updatedMovie.getId());
        
        return convertToDto(updatedMovie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        log.info("Deactivating movie with id: {}", id);
        
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        movie.setActive(false);
        movieRepository.save(movie);
        
        log.info("Movie deactivated successfully: {}", id);
    }

    // Mapping methods
    private MovieDTO convertToDto(Movie movie) {
        return MovieDTO.builder()
            .id(movie.getId())
            .title(movie.getTitle())
            .description(movie.getDescription())
            .genre(movie.getGenre())
            .ageRating(movie.getAgeRating())
            .durationMinutes(movie.getDurationMinutes())
            .director(movie.getDirector())
            .cast(movie.getCast())
            .releaseYear(movie.getReleaseYear())
            .posterPath(movie.getPosterPath())
            .trailerUrl(movie.getTrailerUrl())
            .active(movie.getActive())
            .createdAt(movie.getCreatedAt())
            .updatedAt(movie.getUpdatedAt())
            .imagePaths(movie.getImages().stream()
                .map(MovieImage::getImagePath)
                .collect(Collectors.toSet()))
            .build();
    }

    private Movie convertToEntity(MovieDTO dto) {
        return Movie.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .genre(dto.getGenre())
            .ageRating(dto.getAgeRating())
            .durationMinutes(dto.getDurationMinutes())
            .director(dto.getDirector())
            .cast(dto.getCast())
            .releaseYear(dto.getReleaseYear())
            .posterPath(dto.getPosterPath())
            .trailerUrl(dto.getTrailerUrl())
            .active(dto.getActive() != null ? dto.getActive() : true)
            .build();
    }

    private void updateEntityFromDto(Movie movie, MovieDTO dto) {
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setGenre(dto.getGenre());
        movie.setAgeRating(dto.getAgeRating());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        movie.setReleaseYear(dto.getReleaseYear());
        movie.setPosterPath(dto.getPosterPath());
        movie.setTrailerUrl(dto.getTrailerUrl());
    }
}
