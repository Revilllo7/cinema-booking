package com.cinema.repository;

import com.cinema.entity.Booking;
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
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.screening.id = :screeningId")
    List<Booking> findByScreeningId(@Param("screeningId") Long screeningId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    Page<Booking> findByStatus(@Param("status") Booking.BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Booking> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.screening.id = :screeningId")
    Long countConfirmedBookingsByScreeningId(@Param("screeningId") Long screeningId);
}
