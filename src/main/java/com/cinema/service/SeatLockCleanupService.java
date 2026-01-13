package com.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatLockCleanupService {

    private final SeatReservationService seatReservationService;

    @Scheduled(fixedDelayString = "${app.seating.cleanup-interval-ms:60000}")
    public void cleanupExpiredLocks() {
        log.trace("Running seat lock cleanup");
        seatReservationService.expireLocks();
    }
}
