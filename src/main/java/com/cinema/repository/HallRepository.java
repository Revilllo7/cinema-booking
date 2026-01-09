package com.cinema.repository;

import com.cinema.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    List<Hall> findByActiveTrueOrderByName();

    Optional<Hall> findByName(String name);

    Optional<Hall> findByIdAndActiveTrue(Long id);

    @Query("SELECT h FROM Hall h WHERE h.active = true AND h.totalSeats >= :minSeats")
    List<Hall> findAvailableHallsWithMinSeats(int minSeats);
}
