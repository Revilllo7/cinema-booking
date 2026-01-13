package com.cinema.repository;

import com.cinema.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    List<BookingSeat> findByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.seat.id = :seatId AND bs.booking.screening.id = :screeningId")
    List<BookingSeat> findBySeatIdAndScreeningId(@Param("seatId") Long seatId, @Param("screeningId") Long screeningId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.screening.id = :screeningId AND bs.seatStatus = 'OCCUPIED'")
    List<BookingSeat> findOccupiedSeatsByScreeningId(@Param("screeningId") Long screeningId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.screening.id = :screeningId AND bs.booking.status <> 'CANCELLED'")
    List<BookingSeat> findActiveSeatsByScreeningId(@Param("screeningId") Long screeningId);
}
