package com.cinema.controller.rest;

import com.cinema.dto.ScreeningDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.exception.ScreeningConflictException;
import com.cinema.fixtures.ControllerTestFixtures;
import com.cinema.service.ScreeningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ScreeningRestController.
 * Tests screening endpoints with authorization and error handling.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("ScreeningRestController Tests")
class ScreeningRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @MockBean
    private ScreeningService screeningService;

    private ScreeningDTO validScreeningDTO;

    @BeforeEach
    void setUp() {
        validScreeningDTO = ControllerTestFixtures.createValidScreeningDTOForCreation();
    }

    @Nested
    @DisplayName("GET /api/v1/screenings")
    class GetAllScreenings {

        @Test
        @DisplayName("Should return all screenings")
        void getAllScreenings_ReturnsAll() throws Exception {
            mockMvc.perform(get("/api/v1/screenings")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(screeningService).getAllActiveScreenings(any(), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void getAllScreenings_IsPublic() throws Exception {
            mockMvc.perform(get("/api/v1/screenings")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

                        verify(screeningService).getAllActiveScreenings(any(), isNull(), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/screenings/{id}")
    class GetScreeningById {

        @Test
        @DisplayName("Should return screening by ID")
        void getScreeningById_ReturnsScreening() throws Exception {
            ScreeningDTO screening = validScreeningDTO;
            screening.setId(1L);

            when(screeningService.getScreeningById(1L))
                    .thenReturn(screening);

            mockMvc.perform(get("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(screeningService).getScreeningById(1L);
        }

        @Test
        @DisplayName("Should throw exception when screening not found")
        void getScreeningById_NotFound() throws Exception {
            when(screeningService.getScreeningById(999L))
                    .thenThrow(new ResourceNotFoundException("Screening not found"));

            mockMvc.perform(get("/api/v1/screenings/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(screeningService).getScreeningById(999L);
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void getScreeningById_IsPublic() throws Exception {
            when(screeningService.getScreeningById(1L))
                    .thenReturn(validScreeningDTO);

            mockMvc.perform(get("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(screeningService).getScreeningById(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/screenings/movie/{movieId}")
    class GetScreeningsByMovie {

        @Test
        @DisplayName("Should return screenings by movie")
        void getScreeningsByMovie_ReturnsScreenings() throws Exception {
            ScreeningDTO screening = validScreeningDTO;
            screening.setMovieId(1L);

            when(screeningService.getScreeningsByMovie(1L))
                    .thenReturn(List.of(screening));

            mockMvc.perform(get("/api/v1/screenings/movie/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(screeningService).getScreeningsByMovie(1L);
        }

        @Test
        @DisplayName("Should return empty list when no screenings for movie")
        void getScreeningsByMovie_EmptyList() throws Exception {
            when(screeningService.getScreeningsByMovie(999L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/screenings/movie/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(screeningService).getScreeningsByMovie(999L);
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void getScreeningsByMovie_IsPublic() throws Exception {
            when(screeningService.getScreeningsByMovie(1L))
                    .thenReturn(List.of(validScreeningDTO));

            mockMvc.perform(get("/api/v1/screenings/movie/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/screenings/hall/{hallId}")
    class GetScreeningsByHall {

        @Test
        @DisplayName("Should return screenings by hall")
        void getScreeningsByHall_ReturnsScreenings() throws Exception {
            ScreeningDTO screening = validScreeningDTO;
            screening.setHallId(1L);

            when(screeningService.getScreeningsByHall(1L))
                    .thenReturn(List.of(screening));

            mockMvc.perform(get("/api/v1/screenings/hall/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(screeningService).getScreeningsByHall(1L);
        }

        @Test
        @DisplayName("Should return empty list when no screenings in hall")
        void getScreeningsByHall_EmptyList() throws Exception {
            when(screeningService.getScreeningsByHall(999L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/screenings/hall/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(screeningService).getScreeningsByHall(999L);
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void getScreeningsByHall_IsPublic() throws Exception {
            when(screeningService.getScreeningsByHall(1L))
                    .thenReturn(List.of(validScreeningDTO));

            mockMvc.perform(get("/api/v1/screenings/hall/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/screenings")
    class CreateScreening {

        @Test
        @DisplayName("Admin can create screening")
        @WithMockUser(roles = "ADMIN")
        void createScreening_AsAdmin_Success() throws Exception {
            ScreeningDTO created = validScreeningDTO;
            created.setId(1L);

            when(screeningService.createScreening(any(ScreeningDTO.class)))
                    .thenReturn(created);

            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(screeningService).createScreening(any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("User cannot create screening")
        @WithMockUser(roles = "USER")
        void createScreening_AsUser_Forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isForbidden());

            verify(screeningService, never()).createScreening(any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Should return 409 Conflict when screening time conflicts")
        @WithMockUser(roles = "ADMIN")
        void createScreening_TimeConflict() throws Exception {
            when(screeningService.createScreening(any(ScreeningDTO.class)))
                    .thenThrow(new ScreeningConflictException(
                            "Screening time conflicts with existing screening"));

            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isConflict());

            verify(screeningService).createScreening(any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Anonymous cannot create screening")
        void createScreening_Anonymous_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isUnauthorized());

            verify(screeningService, never()).createScreening(any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Should reject screening with invalid base price")
        @WithMockUser(roles = "ADMIN")
        void createScreening_InvalidBasePrice() throws Exception {
            ScreeningDTO invalid = validScreeningDTO;
            invalid.setBasePrice(-10.0);

            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(screeningService, never()).createScreening(any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Should reject screening with null start time")
        @WithMockUser(roles = "ADMIN")
        void createScreening_NullStartTime() throws Exception {
            ScreeningDTO invalid = validScreeningDTO;
            invalid.setStartTime(null);

            mockMvc.perform(post("/api/v1/screenings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(invalid)))
                    .andExpect(status().isBadRequest());

            verify(screeningService, never()).createScreening(any(ScreeningDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/screenings/{id}")
    class UpdateScreening {

        @Test
        @DisplayName("Admin can update screening")
        @WithMockUser(roles = "ADMIN")
        void updateScreening_AsAdmin_Success() throws Exception {
            ScreeningDTO updated = validScreeningDTO;
            updated.setId(1L);
            updated.setBasePrice(20.0);

            when(screeningService.updateScreening(eq(1L), any(ScreeningDTO.class)))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.basePrice").value(20.0));

            verify(screeningService).updateScreening(eq(1L), any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("User cannot update screening")
        @WithMockUser(roles = "USER")
        void updateScreening_AsUser_Forbidden() throws Exception {
            mockMvc.perform(put("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isForbidden());

            verify(screeningService, never()).updateScreening(anyLong(), any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when screening not found")
        @WithMockUser(roles = "ADMIN")
        void updateScreening_NotFound() throws Exception {
            when(screeningService.updateScreening(eq(999L), any(ScreeningDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Screening not found"));

            mockMvc.perform(put("/api/v1/screenings/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isNotFound());

            verify(screeningService).updateScreening(eq(999L), any(ScreeningDTO.class));
        }

        @Test
        @DisplayName("Should return 409 Conflict on time conflict during update")
        @WithMockUser(roles = "ADMIN")
        void updateScreening_TimeConflict() throws Exception {
            when(screeningService.updateScreening(eq(1L), any(ScreeningDTO.class)))
                    .thenThrow(new ScreeningConflictException(
                            "New screening time conflicts with existing screening"));

            mockMvc.perform(put("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validScreeningDTO)))
                    .andExpect(status().isConflict());

            verify(screeningService).updateScreening(eq(1L), any(ScreeningDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/screenings/{id}")
    class DeleteScreening {

        @Test
        @DisplayName("Admin can delete screening")
        @WithMockUser(roles = "ADMIN")
        void deleteScreening_AsAdmin_Success() throws Exception {
            doNothing().when(screeningService).deleteScreening(1L);

            mockMvc.perform(delete("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(screeningService).deleteScreening(1L);
        }

        @Test
        @DisplayName("User cannot delete screening")
        @WithMockUser(roles = "USER")
        void deleteScreening_AsUser_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(screeningService, never()).deleteScreening(anyLong());
        }

        @Test
        @DisplayName("Should return 404 when screening not found")
        @WithMockUser(roles = "ADMIN")
        void deleteScreening_NotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Screening not found"))
                    .when(screeningService).deleteScreening(999L);

            mockMvc.perform(delete("/api/v1/screenings/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(screeningService).deleteScreening(999L);
        }

        @Test
        @DisplayName("Anonymous cannot delete screening")
        void deleteScreening_Anonymous_Unauthorized() throws Exception {
            mockMvc.perform(delete("/api/v1/screenings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(screeningService, never()).deleteScreening(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/screenings/hall/{hallId}/date-range")
    class GetScreeningsByHallAndDateRange {

        @Test
        @DisplayName("Should return screenings for date range")
        void getScreeningsByHallAndDateRange_ReturnsScreenings() throws Exception {
            ScreeningDTO screening = validScreeningDTO;
            screening.setHallId(1L);

            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(8);

            when(screeningService.getScreeningsByHallAndDateRange(
                    1L, startDate, endDate))
                    .thenReturn(List.of(screening));

            mockMvc.perform(get("/api/v1/screenings/hall/1/date-range")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(screeningService).getScreeningsByHallAndDateRange(
                    1L, startDate, endDate);
        }

        @Test
        @DisplayName("Should return empty list when no screenings in date range")
        void getScreeningsByHallAndDateRange_EmptyList() throws Exception {
            LocalDateTime startDate = LocalDateTime.now().plusDays(100);
            LocalDateTime endDate = LocalDateTime.now().plusDays(107);

            when(screeningService.getScreeningsByHallAndDateRange(
                    1L, startDate, endDate))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/screenings/hall/1/date-range")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void getScreeningsByHallAndDateRange_IsPublic() throws Exception {
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(8);

            when(screeningService.getScreeningsByHallAndDateRange(
                    1L, startDate, endDate))
                    .thenReturn(List.of(validScreeningDTO));

            mockMvc.perform(get("/api/v1/screenings/hall/1/date-range")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
