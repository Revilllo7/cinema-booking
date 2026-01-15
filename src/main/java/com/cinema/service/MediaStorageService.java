package com.cinema.service;

import com.cinema.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStorageService {

    private static final String WEB_PREFIX = "/uploads/";

    private final StorageProperties storageProperties;

    public String storeMovieImage(Long movieId, MultipartFile file) {
        validateFile(file, storageProperties.getMovieImageMaxSize());
        String extension = extractExtension(file);
        if (!storageProperties.isExtensionAllowed(extension)) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        Path movieDir = storageProperties.resolveMovieImagesPath(movieId);
        createDirectoriesIfNeeded(movieDir);

        String fileName = buildFileName(extension);
        Path target = movieDir.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to store movie image", ex);
            throw new IllegalStateException("Could not store image. Please try again.", ex);
        }

        return storageProperties.buildMovieImageWebPath(movieId, fileName);
    }

    public String storeMoviePoster(Long movieId, MultipartFile file) {
        validateFile(file, storageProperties.getPosterMaxSize());
        String extension = extractExtension(file);
        if (!storageProperties.isPosterExtensionAllowed(extension)) {
            throw new IllegalArgumentException("Unsupported poster type: " + extension);
        }

        Path posterDir = storageProperties.getPosterDirPath();
        createDirectoriesIfNeeded(posterDir);
        deleteExistingPosterFiles(movieId);

        Path target = storageProperties.resolvePosterPath(movieId, extension);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to store movie poster", ex);
            throw new IllegalStateException("Could not store poster. Please try again.", ex);
        }

        return storageProperties.buildPosterWebPath(movieId, extension);
    }

    public void deleteMovieImage(String webPath) {
        if (!StringUtils.hasText(webPath)) {
            return;
        }
        String relativePath = webPath.startsWith(WEB_PREFIX)
            ? webPath.substring(WEB_PREFIX.length())
            : webPath;
        Path absolutePath = storageProperties.getRootPath().resolve(relativePath).normalize();
        try {
            Files.deleteIfExists(absolutePath);
        } catch (IOException ex) {
            log.warn("Could not delete image {}", absolutePath, ex);
        }
    }

    private void validateFile(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File upload is required");
        }
        if (file.getSize() > maxSize) {
            long limitMb = Math.max(1, maxSize / (1024 * 1024));
            throw new IllegalArgumentException("File exceeds maximum size of " + limitMb + " MB");
        }
    }

    private void createDirectoriesIfNeeded(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create directory for uploads", ex);
        }
    }

    private String extractExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return extension != null ? extension.toLowerCase() : "";
    }

    private String buildFileName(String extension) {
        return "img-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "." + extension;
    }

    private void deleteExistingPosterFiles(Long movieId) {
        if (movieId == null) {
            return;
        }
        Path posterDir = storageProperties.getPosterDirPath();
        List<String> extensions = storageProperties.getPosterAllowedExtensions();
        for (String ext : extensions) {
            if (!StringUtils.hasText(ext)) {
                continue;
            }
            Path candidate = posterDir.resolve(movieId + "." + ext.toLowerCase());
            try {
                Files.deleteIfExists(candidate);
            } catch (IOException ex) {
                log.warn("Could not remove old poster {}", candidate, ex);
            }
        }
    }

    public List<String> listMovieImages(Long movieId) {
        Path movieDir = storageProperties.resolveMovieImagesPath(movieId);
        if (!Files.exists(movieDir)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(movieDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> storageProperties.isExtensionAllowed(FilenameUtils.getExtension(path.getFileName().toString())))
                .sorted()
                .map(path -> storageProperties.buildMovieImageWebPath(movieId, path.getFileName().toString()))
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.warn("Failed to read gallery for movie {}", movieId, ex);
            return List.of();
        }
    }
}
