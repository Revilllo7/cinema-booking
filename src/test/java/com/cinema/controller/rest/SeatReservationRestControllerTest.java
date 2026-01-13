package com.cinema.controller.rest;

import com.cinema.config.SecurityConfig;
import com.cinema.dto.SeatStatusDTO;
import com.cinema.service.SeatReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatReservationRestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SeatReservationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatReservationService seatReservationService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "jane")
    void lockSeat_ReturnsSeatStatus() throws Exception {
        SeatStatusDTO seatStatus = SeatStatusDTO.builder()
            .seatId(9L)
            .rowNumber(1)
            .seatNumber(2)
            .status(SeatStatusDTO.SeatState.BOOKED)
            .selectedByYou(true)
            .lockExpiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
        given(seatReservationService.lockSeat(anyLong(), anyLong(), anyString(), anyString())).willReturn(seatStatus);

        SeatReservationRestController.SeatLockRequest request = new SeatReservationRestController.SeatLockRequest(9L);
        mockMvc.perform(post("/api/v1/screenings/4/locks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.seatId").value(9));

        then(seatReservationService).should().lockSeat(anyLong(), eq(9L), anyString(), eq("jane"));
    }

    @Test
    @WithMockUser(username = "jane")
    void releaseSeat_CallsService() throws Exception {
        mockMvc.perform(delete("/api/v1/screenings/4/locks/9").with(csrf()))
            .andExpect(status().isNoContent());

        then(seatReservationService).should().releaseSeat(anyLong(), eq(9L), anyString(), eq("jane"));
    }

    @Test
    @WithMockUser
    void releaseAll_ClearsSessionLocks() throws Exception {
        mockMvc.perform(delete("/api/v1/screenings/4/locks").with(csrf()))
            .andExpect(status().isNoContent());

        then(seatReservationService).should().releaseAll(eq(4L), anyString());
    }
}
