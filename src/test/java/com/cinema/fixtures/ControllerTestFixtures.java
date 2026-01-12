package com.cinema.fixtures;

import com.cinema.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides test data and utilities for REST and web controller tests.
 * Extends EntityFixtures and DTOFixtures with additional controller-specific helpers.
 */
public class ControllerTestFixtures {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ========== UserDTO Fixtures for Controllers ==========

    public static UserDTO createValidUserDTOForRegistration() {
        return UserDTO.builder()
            .username("newuser123")
            .email("newuser@example.com")
            .password("SecurePass123!")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+48123456789")
            .enabled(true)
            .build();
    }

    public static UserDTO createDuplicateUserDTO() {
        return UserDTO.builder()
            .username("testuser") // Duplicate
            .email("test@example.com") // Duplicate
            .password("SecurePass123!")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+48123456789")
            .enabled(true)
            .build();
    }

    public static UserDTO createInvalidUserDTOForController() {
        return UserDTO.builder()
            .username("") // Empty - will fail validation
            .email("invalid-email") // Invalid format
            .password("123") // Too short
            .firstName("")
            .lastName("")
            .build();
    }

    // ========== MovieDTO Fixtures for Controllers ==========

    public static MovieDTO createValidMovieDTOForCreation() {
        return MovieDTO.builder()
            .title("The Dark Knight")
            .description("A vigilante fights crime in Gotham City")
            .genre("Action")
            .ageRating("PG-13")
            .durationMinutes(152)
            .director("Christopher Nolan")
            .cast("Christian Bale, Heath Ledger")
            .releaseYear(2008)
            .posterPath("/posters/dark-knight.jpg")
            .trailerUrl("https://youtube.com/watch?v=EXeTwQWrcwY")
            .active(true)
            .build();
    }

    public static MovieDTO createInvalidMovieDTOForController() {
        return MovieDTO.builder()
            .title("") // Empty title
            .description("")
            .durationMinutes(-1) // Negative
            .releaseYear(1800) // Invalid year
            .build();
    }

    public static MovieDTO createMovieDTOForUpdate() {
        return MovieDTO.builder()
            .title("The Dark Knight Rises")
            .description("Updated description")
            .genre("Action")
            .ageRating("PG-13")
            .durationMinutes(165)
            .director("Christopher Nolan")
            .cast("Christian Bale, Tom Hardy")
            .releaseYear(2012)
            .active(true)
            .build();
    }

    // ========== ScreeningDTO Fixtures for Controllers ==========

    public static ScreeningDTO createValidScreeningDTOForCreation() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(7).withHour(19).withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(150);

        return ScreeningDTO.builder()
            .movieId(1L)
            .hallId(1L)
            .startTime(startTime)
            .endTime(endTime)
            .basePrice(29.99)
            .active(true)
            .build();
    }

    public static ScreeningDTO createScreeningDTOWithConflict() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(150);

        return ScreeningDTO.builder()
            .movieId(1L)
            .hallId(1L)
            .startTime(startTime)
            .endTime(endTime)
            .basePrice(29.99)
            .active(true)
            .build();
    }

    public static ScreeningDTO createScreeningDTOForUpdate() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(14).withHour(20).withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(165);

        return ScreeningDTO.builder()
            .movieId(1L)
            .hallId(2L)
            .startTime(startTime)
            .endTime(endTime)
            .basePrice(34.99)
            .active(true)
            .build();
    }

    public static ScreeningDTO createInvalidScreeningDTOForController() {
        return ScreeningDTO.builder()
            .movieId(null) // Null
            .hallId(null) // Null
            .startTime(null)
            .endTime(null)
            .basePrice(-10.0) // Negative
            .build();
    }

    // ========== BookingDTO Fixtures for Controllers ==========

    public static BookingDTO createValidBookingDTOForCreation() {
        BookingDTO.BookingSeatRequest seat = new BookingDTO.BookingSeatRequest(1L, 1L);

        return BookingDTO.builder()
            .userId(1L)
            .screeningId(1L)
            .seats(java.util.List.of(seat))
            .totalPrice(59.98)
            .status("PENDING")
            .customerEmail("user@example.com")
            .customerPhone("+48123456789")
            .build();
    }

    public static BookingDTO createBookingDTOWithInvalidSeats() {
        return BookingDTO.builder()
            .userId(1L)
            .screeningId(1L)
            .totalPrice(0.0)
            .build();
    }

    public static BookingDTO createInvalidBookingDTOForController() {
        return BookingDTO.builder()
            .userId(null) // Null
            .screeningId(null) // Null
            .build();
    }

    // ========== Utility Methods ==========

    /**
     * Convert object to JSON string for request body
     */
    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Create a set of user roles for testing
     */
    public static Set<String> createUserRoles() {
        return new HashSet<>(Set.of("ROLE_USER"));
    }

    /**
     * Create a set of admin roles for testing
     */
    public static Set<String> createAdminRoles() {
        return new HashSet<>(Set.of("ROLE_ADMIN"));
    }

    /**
     * Create a set of both user and admin roles
     */
    public static Set<String> createAllRoles() {
        return new HashSet<>(Set.of("ROLE_USER", "ROLE_ADMIN"));
    }
}
