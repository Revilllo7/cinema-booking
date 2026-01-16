package com.cinema.controller.rest;

import com.cinema.dto.BookingDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.exception.SeatNotAvailableException;
import com.cinema.fixtures.ControllerTestFixtures;
import com.cinema.fixtures.DTOFixtures;
import com.cinema.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BookingRestController using @SpringBootTest with MockMvc.
 * Tests both happy paths and error scenarios with role-based access control.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("BookingRestController Tests")
class BookingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        bookingDTO = DTOFixtures.createDefaultBookingDTO();
        bookingDTO.setId(1L);
    }

    // ========== GET /api/v1/bookings - Get All Bookings (Admin Only) ==========

    @Nested
    @DisplayName("GET /api/v1/bookings Tests")
    class GetAllBookingsTests {

        @Test
        @DisplayName("Should return all bookings with pagination for admin")
        @WithMockUser(roles = "ADMIN")
        void getAllBookings_AsAdmin_ReturnsPageOfBookings() throws Exception {
            // Given
            Page<BookingDTO> bookingPage = new PageImpl<>(List.of(bookingDTO));
            given(bookingService.getAllBookings(any())).willReturn(bookingPage);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").exists());
        }

        @Test
        @DisplayName("Should deny access to non-admin users")
        @WithMockUser(roles = "USER")
        void getAllBookings_AsUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should redirect unauthenticated users to login")
        @WithAnonymousUser
        void getAllBookings_Unauthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ========== GET /api/v1/bookings/{id} - Get Booking by ID ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/{id} Tests")
    class GetBookingByIdTests {

        @Test
        @DisplayName("Should return booking when found")
        @WithMockUser
        void getBookingById_ExistingId_ReturnsBooking() throws Exception {
            // Given
            given(bookingService.getBookingById(1L)).willReturn(bookingDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.screeningId").exists());

            then(bookingService).should().getBookingById(1L);
        }

        @Test
        @DisplayName("Should return 404 when booking not found")
        @WithMockUser
        void getBookingById_NonExistingId_ReturnsNotFound() throws Exception {
            // Given
            given(bookingService.getBookingById(999L))
                .willThrow(new ResourceNotFoundException("Booking", "id", 999L));

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should allow public access to booking details")
        @WithAnonymousUser
        void getBookingById_AnonymousUser_ReturnsOk() throws Exception {
            // Given
            given(bookingService.getBookingById(1L)).willReturn(bookingDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/1"))
                .andExpect(status().isOk());
        }
    }

    // ========== GET /api/v1/bookings/number/{bookingNumber} ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/number/{bookingNumber} Tests")
    class GetBookingByNumberTests {

        @Test
        @DisplayName("Should return booking by booking number")
        @WithMockUser
        void getBookingByNumber_ValidNumber_ReturnsBooking() throws Exception {
            // Given
            String bookingNumber = "BK-2024-00001";
            given(bookingService.getBookingByBookingNumber(bookingNumber))
                .willReturn(bookingDTO);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/number/" + bookingNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("Should return 404 for non-existing booking number")
        @WithMockUser
        void getBookingByNumber_InvalidNumber_ReturnsNotFound() throws Exception {
            // Given
            given(bookingService.getBookingByBookingNumber("INVALID"))
                .willThrow(new ResourceNotFoundException("Booking number not found"));

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/number/INVALID"))
                .andExpect(status().isNotFound());
        }
    }

    // ========== GET /api/v1/bookings/user/{userId} ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/user/{userId} Tests")
    class GetBookingsByUserTests {

        @Test
        @DisplayName("Should return user's bookings with pagination")
        @WithMockUser
        void getBookingsByUser_ExistingUser_ReturnsPaginatedBookings() throws Exception {
            // Given
            Page<BookingDTO> bookingPage = new PageImpl<>(List.of(bookingDTO));
            given(bookingService.getBookingsByUser(1L, PageRequest.of(0, 10)))
                .willReturn(bookingPage);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/user/1")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").exists());
        }

        @Test
        @DisplayName("Should return empty page when user has no bookings")
        @WithMockUser
        void getBookingsByUser_UserWithoutBookings_ReturnsEmptyPage() throws Exception {
            // Given
            Page<BookingDTO> emptyPage = new PageImpl<>(List.of());
            given(bookingService.getBookingsByUser(999L, PageRequest.of(0, 10)))
                .willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // ========== GET /api/v1/bookings/screening/{screeningId} - Admin Only ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/screening/{screeningId} Tests")
    class GetBookingsByScreeningTests {

        @Test
        @DisplayName("Should return bookings for screening as admin")
        @WithMockUser(roles = "ADMIN")
        void getBookingsByScreening_AsAdmin_ReturnsBookings() throws Exception {
            // Given
            given(bookingService.getBookingsByScreening(1L))
                .willReturn(List.of(bookingDTO));

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/screening/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("Should deny access to non-admin users")
        @WithMockUser(roles = "USER")
        void getBookingsByScreening_AsUser_ReturnsForbidden() throws Exception {
            mockMvc.perform(get("/api/v1/bookings/screening/1"))
                .andExpect(status().isForbidden());
        }
    }

    // ========== GET /api/v1/bookings/status/{status} - Admin Only ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/status/{status} Tests")
    class GetBookingsByStatusTests {

        @Test
        @DisplayName("Should return bookings filtered by status as admin")
        @WithMockUser(roles = "ADMIN")
        void getBookingsByStatus_AsAdmin_ReturnsFilteredBookings() throws Exception {
            // Given
            Page<BookingDTO> bookingPage = new PageImpl<>(List.of(bookingDTO));
            given(bookingService.getBookingsByStatus(any(), any()))
                .willReturn(bookingPage);

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/status/CONFIRMED"))
                .andExpect(status().isOk());
        }
    }

    // ========== GET /api/v1/bookings/date-range - Admin Only ==========

    @Nested
    @DisplayName("GET /api/v1/bookings/date-range Tests")
    class GetBookingsByDateRangeTests {

        @Test
        @DisplayName("Should return bookings within date range as admin")
        @WithMockUser(roles = "ADMIN")
        void getBookingsByDateRange_AsAdmin_ReturnsBookings() throws Exception {
            // Given
            given(bookingService.getBookingsByDateRange(any(), any()))
                .willReturn(List.of(bookingDTO));

            // When & Then
            mockMvc.perform(get("/api/v1/bookings/date-range")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());
        }
    }

    // ========== POST /api/v1/bookings - Create Booking ==========

    @Nested
    @DisplayName("POST /api/v1/bookings Tests")
    class CreateBookingTests {

        @Test
        @DisplayName("Should create booking and return 201")
        @WithMockUser
        void createBooking_ValidData_ReturnsCreated() throws Exception {
            // Given
            BookingDTO newBooking = ControllerTestFixtures.createValidBookingDTOForCreation();
            BookingDTO savedBooking = newBooking;
            savedBooking.setId(1L);

            given(bookingService.createBookingForUser(anyString(), any(BookingDTO.class)))
                .willReturn(savedBooking);

            // When & Then
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newBooking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

            then(bookingService).should().createBookingForUser(anyString(), any(BookingDTO.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid booking data")
        @WithMockUser
        void createBooking_InvalidData_ReturnsBadRequest() throws Exception {
            // Given
            BookingDTO invalidBooking = ControllerTestFixtures.createInvalidBookingDTOForController();

            // When & Then
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBooking)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when seats not available")
        @WithMockUser
        void createBooking_SeatsNotAvailable_ReturnsBadRequest() throws Exception {
            // Given
            BookingDTO newBooking = ControllerTestFixtures.createValidBookingDTOForCreation();
            given(bookingService.createBookingForUser(anyString(), any(BookingDTO.class)))
                .willThrow(new SeatNotAvailableException("Selected seats are not available"));

            // When & Then
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newBooking)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated requests")
        @WithAnonymousUser
        void createBooking_Unauthenticated_ReturnsUnauthorized() throws Exception {
            // Given
            BookingDTO newBooking = ControllerTestFixtures.createValidBookingDTOForCreation();

            // When & Then
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newBooking)))
                .andExpect(status().isUnauthorized());
        }
    }

    // ========== PUT /api/v1/bookings/{id}/confirm ==========

    @Nested
    @DisplayName("PUT /api/v1/bookings/{id}/confirm Tests")
    class ConfirmBookingTests {

        @Test
        @DisplayName("Should confirm booking with valid payment details")
        @WithMockUser
        void confirmBooking_ValidPayment_ReturnsConfirmedBooking() throws Exception {
            // Given
            BookingDTO confirmedBooking = bookingDTO;
            confirmedBooking.setStatus("CONFIRMED");
            given(bookingService.confirmBooking(anyLong(), any(), any()))
                .willReturn(confirmedBooking);

            // When & Then
            mockMvc.perform(put("/api/v1/bookings/1/confirm")
                    .param("paymentMethod", "CREDIT_CARD")
                    .param("paymentReference", "PAY123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("Should return 404 when booking not found")
        @WithMockUser
        void confirmBooking_InvalidBookingId_ReturnsNotFound() throws Exception {
            // Given
            given(bookingService.confirmBooking(anyLong(), any(), any()))
                .willThrow(new ResourceNotFoundException("Booking not found"));

            // When & Then
            mockMvc.perform(put("/api/v1/bookings/999/confirm")
                    .param("paymentMethod", "CREDIT_CARD")
                    .param("paymentReference", "PAY123456"))
                .andExpect(status().isNotFound());
        }
    }

    // ========== PUT /api/v1/bookings/{id}/cancel ==========

    @Nested
    @DisplayName("PUT /api/v1/bookings/{id}/cancel Tests")
    class CancelBookingTests {

        @Test
        @DisplayName("Should cancel booking successfully")
        @WithMockUser
        void cancelBooking_ValidId_ReturnsCancelledBooking() throws Exception {
            // Given
            BookingDTO cancelledBooking = bookingDTO;
            cancelledBooking.setStatus("CANCELLED");
            given(bookingService.cancelBooking(anyLong(), any()))
                .willReturn(cancelledBooking);

            // When & Then
            mockMvc.perform(put("/api/v1/bookings/1/cancel")
                    .param("reason", "User requested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Should return 404 when booking not found")
        @WithMockUser
        void cancelBooking_InvalidId_ReturnsNotFound() throws Exception {
            // Given
            given(bookingService.cancelBooking(anyLong(), any()))
                .willThrow(new ResourceNotFoundException("Booking not found"));

            // When & Then
            mockMvc.perform(put("/api/v1/bookings/999/cancel"))
                .andExpect(status().isNotFound());
        }
    }
}
