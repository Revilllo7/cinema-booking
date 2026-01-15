package com.cinema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Root directory for all uploaded assets. */
    private String rootDir = "uploads";

    /** Folder name used within the root for movie images. */
    private String movieImagesDir = "movies";

    /** Maximum allowed size (in bytes) for a single movie image. */
    private long movieImageMaxSize = 5 * 1024 * 1024; // 5 MB

    /** Allow-listed file extensions for movie images. */
    private List<String> movieImageAllowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "webp"));

    /** Minimum number of gallery images that should remain per movie. */
    private int minMovieImages = 3;

    /** Directory for primary poster artwork. Defaults to the static posters folder. */
    private String posterDir = "src/main/resources/static/images/posters";

    /** Web prefix used when exposing posters. */
    private String posterWebPrefix = "/images/posters";

    /** Maximum allowed poster size (bytes). */
    private long posterMaxSize = 5 * 1024 * 1024; // 5 MB

    /** Allow-listed file extensions for posters. */
    private List<String> posterAllowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "webp"));

    public Path getRootPath() {
        return Paths.get(rootDir).toAbsolutePath().normalize();
    }

    public Path resolveMovieImagesPath(Long movieId) {
        return getRootPath()
            .resolve(movieImagesDir)
            .resolve(String.valueOf(movieId));
    }

    public boolean isExtensionAllowed(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        return movieImageAllowedExtensions.stream()
            .map(String::toLowerCase)
            .anyMatch(ext -> ext.equals(extension.toLowerCase()));
    }

    public String buildMovieImageWebPath(Long movieId, String fileName) {
        return "/uploads/" + movieImagesDir + "/" + movieId + "/" + fileName;
    }

    public Path getPosterDirPath() {
        return Paths.get(posterDir).toAbsolutePath().normalize();
    }

    public Path resolvePosterPath(Long movieId, String extension) {
        return getPosterDirPath().resolve(movieId + "." + extension);
    }

    public boolean isPosterExtensionAllowed(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }
        return posterAllowedExtensions.stream()
            .map(String::toLowerCase)
            .anyMatch(ext -> ext.equals(extension.toLowerCase()));
    }

    public String buildPosterWebPath(Long movieId, String extension) {
        return posterWebPrefix + "/" + movieId + "." + extension;
    }
}
