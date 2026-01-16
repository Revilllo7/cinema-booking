package com.cinema.service;

import com.cinema.config.StorageProperties;
import com.cinema.dto.MovieImageDTO;
import com.cinema.entity.Movie;
import com.cinema.entity.MovieImage;
import com.cinema.repository.MovieImageRepository;
import com.cinema.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieImageServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieImageRepository movieImageRepository;
    @Mock
    private MediaStorageService mediaStorageService;

    private StorageProperties storageProperties;

    @InjectMocks
    private MovieImageService movieImageService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setMinMovieImages(3);
        movieImageService = new MovieImageService(movieRepository, movieImageRepository, mediaStorageService, storageProperties);
        movie = Movie.builder().id(4L).title("Inception").build();
    }

    @Test
    void getGallery_WhenMovieExists_ReturnsOrderedDtos() {
        MovieImage first = MovieImage.builder().id(1L).movie(movie).imagePath("/uploads/1").displayOrder(1).build();
        MovieImage second = MovieImage.builder().id(2L).movie(movie).imagePath("/uploads/2").displayOrder(2).caption("BTS").build();

        when(movieRepository.findById(4L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.findByMovieIdOrderByDisplayOrder(4L)).thenReturn(List.of(first, second));

        List<MovieImageDTO> result = movieImageService.getGallery(4L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getImagePath()).isEqualTo("/uploads/1");
        assertThat(result.get(1).getCaption()).isEqualTo("BTS");
    }

    @Test
    void addImages_PersistsEachFileWithSequentialOrder() {
        MockMultipartFile fileOne = new MockMultipartFile("files", "one.png", "image/png", new byte[]{1});
        MockMultipartFile fileTwo = new MockMultipartFile("files", "two.png", "image/png", new byte[]{2});

        when(movieRepository.findById(4L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.countByMovieId(4L)).thenReturn(1L);
        when(mediaStorageService.storeMovieImage(eq(4L), any())).thenReturn("/uploads/4/movies/img.png");

        AtomicLong idSequence = new AtomicLong(10);
        when(movieImageRepository.save(any(MovieImage.class))).thenAnswer(invocation -> {
            MovieImage entity = invocation.getArgument(0);
            entity.setId(idSequence.getAndIncrement());
            return entity;
        });

        List<MovieImageDTO> created = movieImageService.addImages(4L, List.of(fileOne, fileTwo), List.of("First", "Second"));

        assertThat(created).hasSize(2);
        assertThat(created.get(0).getDisplayOrder()).isEqualTo(2);
        assertThat(created.get(1).getDisplayOrder()).isEqualTo(3);
        verify(mediaStorageService).storeMovieImage(4L, fileOne);
        verify(mediaStorageService).storeMovieImage(4L, fileTwo);
    }

    @Test
    void deleteImage_WhenBelowMinimum_ThrowsException() {
        MovieImage image = MovieImage.builder().id(5L).movie(movie).imagePath("/uploads/5").build();
        when(movieImageRepository.findById(5L)).thenReturn(Optional.of(image));
        when(movieImageRepository.countByMovieId(4L)).thenReturn(3L);

        assertThatThrownBy(() -> movieImageService.deleteImage(4L, 5L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("keep at least");
        verify(mediaStorageService, never()).deleteMovieImage(any());
    }

    @Test
    void deleteImage_WhenValid_RemovesImageAndResequences() {
        MovieImage image = MovieImage.builder().id(5L).movie(movie).imagePath("/uploads/5").displayOrder(2).build();
        when(movieImageRepository.findById(5L)).thenReturn(Optional.of(image));
        when(movieImageRepository.countByMovieId(4L)).thenReturn(5L);
        MovieImage remaining = MovieImage.builder().id(6L).movie(movie).imagePath("/uploads/6").displayOrder(1).build();
        when(movieImageRepository.findByMovieIdOrderByDisplayOrder(4L)).thenReturn(List.of(remaining));

        movieImageService.deleteImage(4L, 5L);

        verify(movieImageRepository).delete(image);
        verify(mediaStorageService).deleteMovieImage("/uploads/5");
        assertThat(remaining.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void deleteImage_WhenImageBelongsToDifferentMovie_Throws() {
        Movie otherMovie = Movie.builder().id(9L).title("Other").build();
        MovieImage image = MovieImage.builder().id(5L).movie(otherMovie).imagePath("/uploads/5").build();
        when(movieImageRepository.findById(5L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> movieImageService.deleteImage(4L, 5L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("does not belong");
    }
}
