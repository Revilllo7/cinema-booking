package com.cinema.repository;

import com.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND m.genre = :genre")
    Page<Movie> findByGenre(@Param("genre") String genre, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND m.releaseYear = :year")
    List<Movie> findByReleaseYear(@Param("year") Integer year);

    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.active = true ORDER BY m.genre")
    List<String> findAllGenres();

    Optional<Movie> findByIdAndActiveTrue(Long id);

    @Query("SELECT m FROM Movie m JOIN m.screenings s WHERE s.id = :screeningId")
    Optional<Movie> findByScreeningId(@Param("screeningId") Long screeningId);
}
