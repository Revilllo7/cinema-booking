package com.cinema.repository;

import com.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.active = true ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByHallIdAndActiveTrue(@Param("hallId") Long hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.rowNumber = :rowNumber AND s.seatNumber = :seatNumber")
    Optional<Seat> findByHallIdAndRowAndSeat(
        @Param("hallId") Long hallId, 
        @Param("rowNumber") Integer rowNumber, 
        @Param("seatNumber") Integer seatNumber
    );

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.seatType = :seatType AND s.active = true")
    List<Seat> findByHallIdAndSeatType(@Param("hallId") Long hallId, @Param("seatType") Seat.SeatType seatType);
}
