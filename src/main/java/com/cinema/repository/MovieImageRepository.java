package com.cinema.repository;

import com.cinema.entity.MovieImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Long> {

    @Query("SELECT mi FROM MovieImage mi WHERE mi.movie.id = :movieId ORDER BY mi.displayOrder")
    List<MovieImage> findByMovieIdOrderByDisplayOrder(@Param("movieId") Long movieId);

    void deleteByMovieId(Long movieId);
}
