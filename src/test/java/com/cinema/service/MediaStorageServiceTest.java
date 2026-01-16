package com.cinema.service;

import com.cinema.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaStorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageProperties storageProperties;
    private MediaStorageService mediaStorageService;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setRootDir(tempDir.toString());
        storageProperties.setMovieImagesDir("movies");
        mediaStorageService = new MediaStorageService(storageProperties);
    }

    @Test
    void storeMovieImage_WithValidFile_PersistsAndReturnsWebPath() throws IOException {
        MockMultipartFile file = new MockMultipartFile("files", "poster.png", "image/png", new byte[]{1, 2, 3});

        String webPath = mediaStorageService.storeMovieImage(9L, file);

        assertThat(webPath).startsWith("/uploads/movies/9/");
        Path storedFile = storageProperties.resolveMovieImagesPath(9L).resolve(Path.of(webPath).getFileName());
        assertThat(Files.exists(storedFile)).isTrue();
    }

    @Test
    void storeMovieImage_WhenExtensionNotAllowed_Throws() {
        MockMultipartFile file = new MockMultipartFile("files", "poster.txt", "text/plain", new byte[]{1});

        assertThatThrownBy(() -> mediaStorageService.storeMovieImage(9L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported file type");
    }

    @Test
    void storeMovieImage_WhenFileTooLarge_Throws() {
        storageProperties.setMovieImageMaxSize(1);
        MockMultipartFile file = new MockMultipartFile("files", "poster.png", "image/png", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> mediaStorageService.storeMovieImage(9L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("maximum size");
    }

    @Test
    void deleteMovieImage_RemovesFileIfExists() throws IOException {
        Path movieDir = storageProperties.resolveMovieImagesPath(9L);
        Files.createDirectories(movieDir);
        Path file = movieDir.resolve("img.png");
        Files.writeString(file, "data");

        mediaStorageService.deleteMovieImage("/uploads/movies/9/img.png");

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void deleteMovieImage_WhenFileDoesNotExist_DoesNotThrow() {
        assertThatCode(() -> mediaStorageService.deleteMovieImage("/uploads/movies/999/nonexistent.png"))
            .doesNotThrowAnyException();
    }

    @Test
    void storeMovieImage_WithEmptyFile_Throws() {
        MockMultipartFile emptyFile = new MockMultipartFile("files", "poster.png", "image/png", new byte[]{});

        assertThatThrownBy(() -> mediaStorageService.storeMovieImage(9L, emptyFile))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storeMovieImage_WithNullFile_Throws() {
        assertThatThrownBy(() -> mediaStorageService.storeMovieImage(9L, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File upload is required");
    }

    @Test
    void storeMovieImage_CreatesDirectoryIfNotExists() throws IOException {
        MockMultipartFile file = new MockMultipartFile("files", "poster.png", "image/png", new byte[]{1, 2, 3});

        Path movieDir = storageProperties.resolveMovieImagesPath(9L);
        assertThat(Files.exists(movieDir)).isFalse();

        String webPath = mediaStorageService.storeMovieImage(9L, file);

        assertThat(webPath).isNotBlank();
        assertThat(Files.exists(movieDir)).isTrue();
    }

    @Test
    void storeMovieImage_WithJpgExtension_Succeeds() throws IOException {
        MockMultipartFile file = new MockMultipartFile("files", "poster.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String webPath = mediaStorageService.storeMovieImage(9L, file);

        assertThat(webPath).startsWith("/uploads/movies/9/");
        assertThat(webPath).endsWith(".jpg");
    }

    @Test
    void storeMovieImage_WithGifExtension_Throws() {
        MockMultipartFile file = new MockMultipartFile("files", "poster.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> mediaStorageService.storeMovieImage(9L, file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported file type");
    }

    @Test
    void storeMovieImage_WithWebpExtension_Succeeds() throws IOException {
        MockMultipartFile file = new MockMultipartFile("files", "poster.webp", "image/webp", new byte[]{1, 2, 3});

        String webPath = mediaStorageService.storeMovieImage(9L, file);

        assertThat(webPath).startsWith("/uploads/movies/9/");
    }

    @Test
    void deleteMovieImage_WithValidPath_RemovesFile() throws IOException {
        Path movieDir = storageProperties.resolveMovieImagesPath(10L);
        Files.createDirectories(movieDir);
        Path file = movieDir.resolve("test.png");
        Files.writeString(file, "test data");

        mediaStorageService.deleteMovieImage("/uploads/movies/10/test.png");

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void storeMovieImage_MultipleFiles_SameMovie_CreatesUniquePaths() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("files", "poster1.png", "image/png", new byte[]{1});
        MockMultipartFile file2 = new MockMultipartFile("files", "poster1.png", "image/png", new byte[]{2});

        String path1 = mediaStorageService.storeMovieImage(9L, file1);
        String path2 = mediaStorageService.storeMovieImage(9L, file2);

        assertThat(path1).isNotEqualTo(path2);
    }
}
