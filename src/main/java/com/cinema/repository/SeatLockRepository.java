package com.cinema.repository;

import com.cinema.entity.SeatLock;
import com.cinema.entity.SeatLock.SeatLockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.seat.id = :seatId " +
        "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
    Optional<SeatLock> findActiveLock(@Param("screeningId") Long screeningId,
                                      @Param("seatId") Long seatId,
                                      @Param("now") LocalDateTime now);

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
    List<SeatLock> findActiveLocksByScreening(@Param("screeningId") Long screeningId,
                                              @Param("now") LocalDateTime now);

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.seat.id = :seatId " +
        "AND sl.status = 'ACTIVE' AND sl.sessionId = :sessionId AND sl.expiresAt > :now")
    Optional<SeatLock> findActiveLockForSession(@Param("screeningId") Long screeningId,
                                                @Param("seatId") Long seatId,
                                                @Param("sessionId") String sessionId,
                                                @Param("now") LocalDateTime now);

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.sessionId = :sessionId " +
        "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
    List<SeatLock> findActiveLocksForSession(@Param("screeningId") Long screeningId,
                                             @Param("sessionId") String sessionId,
                                             @Param("now") LocalDateTime now);

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.seat.id = :seatId " +
        "AND sl.status = 'ACTIVE' AND sl.username = :username AND sl.expiresAt > :now")
    Optional<SeatLock> findActiveLockForUsername(@Param("screeningId") Long screeningId,
                                                 @Param("seatId") Long seatId,
                                                 @Param("username") String username,
                                                 @Param("now") LocalDateTime now);

    @Query("SELECT sl FROM SeatLock sl WHERE sl.screening.id = :screeningId AND sl.username = :username " +
        "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
    List<SeatLock> findActiveLocksForUsername(@Param("screeningId") Long screeningId,
                                              @Param("username") String username,
                                              @Param("now") LocalDateTime now);

    List<SeatLock> findByStatusAndExpiresAtBefore(SeatLockStatus status, LocalDateTime timestamp);

}
