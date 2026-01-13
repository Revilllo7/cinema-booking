package com.cinema.service;

import com.cinema.dto.SeatStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatStatusNotifierService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Long screeningId, List<SeatStatusDTO> payload) {
        String destination = "/topic/screenings/" + screeningId + "/seats";
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Published seat update to {} ({} seats)", destination, payload.size());
    }
}
