package com.cinema.repository;

import com.cinema.entity.Screening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query("SELECT s FROM Screening s WHERE s.active = true AND s.startTime >= :startDate ORDER BY s.startTime")
    Page<Screening> findUpcomingScreenings(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.active = true AND s.startTime >= :startDate ORDER BY s.startTime")
    List<Screening> findByMovieIdAndStartTimeAfter(@Param("movieId") Long movieId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT s FROM Screening s WHERE s.hall.id = :hallId AND s.active = true AND s.startTime >= :startDate ORDER BY s.startTime")
    List<Screening> findByHallIdAndStartTimeAfter(@Param("hallId") Long hallId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT s FROM Screening s WHERE s.hall.id = :hallId AND DATE(s.startTime) BETWEEN DATE(:startDate) AND DATE(:endDate)")
    List<Screening> findByHallIdAndStartTimeBetween(
        @Param("hallId") Long hallId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT s FROM Screening s WHERE s.active = true AND DATE(s.startTime) = DATE(:date) ORDER BY s.startTime")
    List<Screening> findByDate(@Param("date") LocalDateTime date);

    Optional<Screening> findByIdAndActiveTrue(Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Screening s " +
           "WHERE s.hall.id = :hallId AND s.id != :screeningId AND s.active = true " +
           "AND ((s.startTime BETWEEN :startTime AND :endTime) OR " +
           "(s.endTime BETWEEN :startTime AND :endTime) OR " +
           "(:startTime BETWEEN s.startTime AND s.endTime))")
    boolean existsConflictingScreening(
        @Param("hallId") Long hallId,
        @Param("screeningId") Long screeningId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
