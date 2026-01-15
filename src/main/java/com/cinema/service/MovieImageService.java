package com.cinema.service;

import com.cinema.config.StorageProperties;
import com.cinema.dto.MovieImageDTO;
import com.cinema.entity.Movie;
import com.cinema.entity.MovieImage;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.MovieImageRepository;
import com.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieImageService {

    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;
    private final MediaStorageService mediaStorageService;
    private final StorageProperties storageProperties;

    @Transactional(readOnly = true)
    public List<MovieImageDTO> getGallery(Long movieId) {
        ensureMovieExists(movieId);
        return movieImageRepository.findByMovieIdOrderByDisplayOrder(movieId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public List<MovieImageDTO> addImages(Long movieId, List<MultipartFile> files, List<String> captions) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Please provide at least one image");
        }
        Movie movie = ensureMovieExists(movieId);
        int startingOrder = (int) movieImageRepository.countByMovieId(movieId) + 1;
        AtomicInteger orderCounter = new AtomicInteger(startingOrder);
        List<MovieImageDTO> created = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String caption = captions != null && i < captions.size() ? captions.get(i) : null;
            String webPath = mediaStorageService.storeMovieImage(movieId, file);
            MovieImage image = MovieImage.builder()
                .movie(movie)
                .imagePath(webPath)
                .displayOrder(orderCounter.getAndIncrement())
                .caption(caption)
                .build();
            MovieImage saved = movieImageRepository.save(image);
            created.add(toDto(saved));
        }

        log.info("Uploaded {} image(s) for movie {}", created.size(), movieId);
        return created;
    }

    @Transactional
    public void deleteImage(Long movieId, Long imageId) {
        MovieImage image = movieImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("MovieImage", "id", imageId));

        if (!image.getMovie().getId().equals(movieId)) {
            throw new IllegalArgumentException("Image does not belong to the specified movie");
        }

        long totalImages = movieImageRepository.countByMovieId(movieId);
        if (totalImages <= storageProperties.getMinMovieImages()) {
            throw new IllegalStateException("Each movie must keep at least " + storageProperties.getMinMovieImages() + " gallery images");
        }

        movieImageRepository.delete(image);
        mediaStorageService.deleteMovieImage(image.getImagePath());
        log.info("Deleted movie image {} for movie {}", imageId, movieId);
        resequenceDisplayOrder(movieId);
    }

    private void resequenceDisplayOrder(Long movieId) {
        List<MovieImage> images = movieImageRepository.findByMovieIdOrderByDisplayOrder(movieId);
        AtomicInteger order = new AtomicInteger(1);
        images.forEach(img -> img.setDisplayOrder(order.getAndIncrement()));
        movieImageRepository.saveAll(images);
    }

    private Movie ensureMovieExists(Long movieId) {
        return movieRepository.findById(movieId)
            .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));
    }

    private MovieImageDTO toDto(MovieImage image) {
        return MovieImageDTO.builder()
            .id(image.getId())
            .imagePath(image.getImagePath())
            .displayOrder(image.getDisplayOrder())
            .caption(image.getCaption())
            .build();
    }
}
