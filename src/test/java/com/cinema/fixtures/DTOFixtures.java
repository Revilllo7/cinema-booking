package com.cinema.fixtures;

import com.cinema.dto.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides pre-configured DTO instances for testing.
 * Use these fixtures for consistent test data across all test classes.
 */
public class DTOFixtures {

    // ========== UserDTO Fixtures ==========
    
    public static UserDTO createDefaultUserDTO() {
        return UserDTO.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("SecurePass123!")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+48123456789")
            .enabled(true)
            .roles(new HashSet<>(Set.of("ROLE_USER")))
            .build();
    }

    public static UserDTO createUserDTOWithoutId() {
        return UserDTO.builder()
            .username("newuser")
            .email("new@example.com")
            .password("SecurePass123!")
            .firstName("New")
            .lastName("User")
            .phoneNumber("+48987654321")
            .enabled(true)
            .build();
    }

    public static UserDTO createInvalidUserDTO() {
        return UserDTO.builder()
            .username("ab") // Too short
            .email("invalid-email") // Invalid format
            .password("short") // Too short
            .phoneNumber("123") // Invalid format
            .build();
    }

    // ========== MovieDTO Fixtures ==========
    
    public static MovieDTO createDefaultMovieDTO() {
        return MovieDTO.builder()
            .id(1L)
            .title("Inception")
            .description("A thief who steals corporate secrets through dream-sharing technology")
            .genre("Sci-Fi")
            .ageRating("PG-13")
            .durationMinutes(148)
            .director("Christopher Nolan")
            .cast("Leonardo DiCaprio, Joseph Gordon-Levitt")
            .releaseYear(2010)
            .posterPath("/posters/inception.jpg")
            .trailerUrl("https://youtube.com/watch?v=YoHD9XEInc0")
            .active(true)
            .imagePaths(List.of())
            .createdAt(LocalDateTime.now().minusDays(60))
            .updatedAt(LocalDateTime.now().minusDays(10))
            .build();
    }

    public static MovieDTO createMovieDTOWithoutId() {
        return MovieDTO.builder()
            .title("New Movie")
            .description("A brand new movie for testing")
            .genre("Drama")
            .ageRating("R")
            .durationMinutes(120)
            .director("Test Director")
            .cast("Actor 1, Actor 2")
            .releaseYear(2024)
            .active(true)
            .build();
    }

    public static MovieDTO createInvalidMovieDTO() {
        return MovieDTO.builder()
            .title("") // Empty title
            .durationMinutes(-10) // Negative duration
            .releaseYear(1800) // Invalid year
            .build();
    }

    // ========== ScreeningDTO Fixtures ==========
    
    public static ScreeningDTO createDefaultScreeningDTO() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        return ScreeningDTO.builder()
            .id(1L)
            .movieId(1L)
            .movieTitle("Inception")
            .hallId(1L)
            .hallName("Hall 1")
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2).plusMinutes(30))
            .basePrice(25.0)
            .active(true)
            .availableSeats(100)
            .createdAt(LocalDateTime.now().minusDays(5))
            .build();
    }

    public static ScreeningDTO createScreeningDTOWithoutId() {
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7).withHour(20).withMinute(0);
        return ScreeningDTO.builder()
            .movieId(1L)
            .hallId(1L)
            .startTime(nextWeek)
            .endTime(nextWeek.plusHours(2))
            .basePrice(30.0)
            .active(true)
            .build();
    }

    public static ScreeningDTO createInvalidScreeningDTO() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        return ScreeningDTO.builder()
            .movieId(null) // Missing required field
            .hallId(null) // Missing required field
            .startTime(past) // Past date (fails @Future validation)
            .basePrice(-10.0) // Negative price
            .build();
    }

    // ========== BookingDTO Fixtures ==========
    
    public static BookingDTO createDefaultBookingDTO() {
        List<BookingDTO.BookingSeatRequest> seats = new ArrayList<>();
        seats.add(new BookingDTO.BookingSeatRequest(1L, 1L));
        seats.add(new BookingDTO.BookingSeatRequest(2L, 1L));

        return BookingDTO.builder()
            .id(1L)
            .bookingNumber("BOOK-123456")
            .userId(1L)
            .screeningId(1L)
            .seats(seats)
            .totalPrice(50.0)
            .status("PENDING")
            .paymentMethod("CREDIT_CARD")
            .customerEmail("test@example.com")
            .customerPhone("+48123456789")
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static BookingDTO createBookingDTOWithoutId() {
        List<BookingDTO.BookingSeatRequest> seats = new ArrayList<>();
        seats.add(new BookingDTO.BookingSeatRequest(1L, 1L));

        return BookingDTO.builder()
            .userId(1L)
            .screeningId(1L)
            .seats(seats)
            .customerEmail("newbooking@example.com")
            .customerPhone("+48987654321")
            .build();
    }

    public static BookingDTO createInvalidBookingDTO() {
        return BookingDTO.builder()
            .userId(null) // Missing required field
            .screeningId(null) // Missing required field
            .seats(new ArrayList<>()) // Empty seats list
            .customerEmail("invalid-email") // Invalid email format
            .customerPhone("123") // Invalid phone format
            .build();
    }

    // ========== SeatDTO Fixtures ==========
    
    public static SeatDTO createDefaultSeatDTO() {
        return SeatDTO.builder()
            .id(1L)
            .hallId(1L)
            .rowNumber(5)
            .seatNumber(10)
            .seatType("STANDARD")
            .available(true)
            .build();
    }

    public static SeatDTO createOccupiedSeatDTO() {
        return SeatDTO.builder()
            .id(2L)
            .hallId(1L)
            .rowNumber(5)
            .seatNumber(11)
            .seatType("STANDARD")
            .available(false)
            .build();
    }

    // ========== Builder Methods for Customization ==========
    
    public static UserDTO.UserDTOBuilder userDTOBuilder() {
        return UserDTO.builder()
            .username("testuser")
            .email("test@example.com")
            .password("SecurePass123!")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+48123456789")
            .enabled(true);
    }

    public static MovieDTO.MovieDTOBuilder movieDTOBuilder() {
        return MovieDTO.builder()
            .title("Test Movie")
            .description("Test description")
            .genre("Action")
            .ageRating("PG-13")
            .durationMinutes(120)
            .director("Test Director")
            .cast("Actor 1, Actor 2")
            .releaseYear(2024)
            .active(true);
    }

    public static ScreeningDTO.ScreeningDTOBuilder screeningDTOBuilder() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        return ScreeningDTO.builder()
            .movieId(1L)
            .hallId(1L)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true);
    }

    public static BookingDTO.BookingDTOBuilder bookingDTOBuilder() {
        List<BookingDTO.BookingSeatRequest> seats = new ArrayList<>();
        seats.add(new BookingDTO.BookingSeatRequest(1L, 1L));

        return BookingDTO.builder()
            .userId(1L)
            .screeningId(1L)
            .seats(seats)
            .customerEmail("test@example.com")
            .customerPhone("+48123456789");
    }
}
