package com.cinema.fixtures;

import com.cinema.entity.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Provides pre-configured entity instances for testing.
 * Use these fixtures for consistent test data across all test classes.
 */
public class EntityFixtures {

    // ========== User Fixtures ==========
    
    public static User createDefaultUser() {
        return User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("$2a$10$encoded.password.hash")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+48123456789")
            .enabled(true)
            .roles(new HashSet<>())
            .bookings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static User createAdminUser() {
        return User.builder()
            .username("admin")
            .email("admin@example.com")
            .password("$2a$10$encoded.password.hash")
            .firstName("Admin")
            .lastName("User")
            .phoneNumber("+48123456790")
            .enabled(true)
            .roles(new HashSet<>())  // Empty roles - add roles in test if needed
            .bookings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static User createDisabledUser() {
        return User.builder()
            .username("disabled")
            .email("disabled@example.com")
            .password("$2a$10$encoded.password.hash")
            .firstName("Disabled")
            .lastName("User")
            .phoneNumber("+48123456791")
            .enabled(false)
            .roles(new HashSet<>())
            .bookings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // ========== Role Fixtures ==========
    
    public static Role createUserRole() {
        return Role.builder()
            .name("ROLE_USER")
            .description("Standard user role")
            .build();
    }

    public static Role createAdminRole() {
        return Role.builder()
            .name("ROLE_ADMIN")
            .description("Administrator role")
            .build();
    }

    // ========== Movie Fixtures ==========
    
    public static Movie createDefaultMovie() {
        return Movie.builder()
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
            .images(new HashSet<>())
            .screenings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(60))
            .updatedAt(LocalDateTime.now().minusDays(10))
            .build();
    }

    public static Movie createInactiveMovie() {
        return Movie.builder()
            .title("Old Movie")
            .description("A classic movie")
            .genre("Drama")
            .ageRating("PG")
            .durationMinutes(120)
            .director("Classic Director")
            .cast("Actor A, Actor B")
            .releaseYear(1990)
            .active(false)
            .images(new HashSet<>())
            .screenings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(365))
            .updatedAt(LocalDateTime.now().minusDays(300))
            .build();
    }

    // ========== Hall Fixtures ==========
    
    public static Hall createDefaultHall() {
        return Hall.builder()
            .name("Hall 1")
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .seats(new HashSet<>())
            .screenings(new HashSet<>())
            .build();
    }

    public static Hall createLargeHall() {
        return Hall.builder()
            .name("Hall 2 - IMAX")
            .totalSeats(200)
            .rowsCount(20)
            .seatsPerRow(10)
            .active(true)
            .seats(new HashSet<>())
            .screenings(new HashSet<>())
            .build();
    }

    // ========== Seat Fixtures ==========
    
    /**
     * Creates a standard seat WITHOUT persisting dependencies.
     * WARNING: Hall is transient and must be persisted separately before using this seat.
     * NOTE: This is used by convenience methods; prefer explicit Seat.builder() in tests.
     */
    public static Seat createStandardSeat() {
        return Seat.builder()
            .hall(createDefaultHall())
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .bookingSeats(new HashSet<>())
            .build();
    }

    /**
     * Creates a VIP seat WITHOUT persisting dependencies.
     * WARNING: Hall is transient and must be persisted separately before using this seat.
     * NOTE: This is used by convenience methods; prefer explicit Seat.builder() in tests.
     */
    public static Seat createVIPSeat() {
        return Seat.builder()
            .hall(createDefaultHall())
            .rowNumber(1)
            .seatNumber(5)
            .seatType(Seat.SeatType.VIP)
            .bookingSeats(new HashSet<>())
            .build();
    }

    // ========== Screening Fixtures ==========
    
    /**
     * Creates an upcoming screening WITHOUT persisting dependencies.
     * WARNING: Movie and Hall are transient and must be persisted separately before using this screening.
     * NOTE: This is used by convenience methods; prefer screeningBuilder() or explicit entity creation in tests.
     */
    public static Screening createUpcomingScreening() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        return Screening.builder()
            .movie(createDefaultMovie())
            .hall(createDefaultHall())
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2).plusMinutes(30))
            .basePrice(25.0)
            .active(true)
            .bookings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(5))
            .updatedAt(LocalDateTime.now().minusDays(5))
            .build();
    }

    /**
     * Creates a past screening WITHOUT persisting dependencies.
     * WARNING: Movie and Hall are transient and must be persisted separately before using this screening.
     * NOTE: This is used by convenience methods; prefer screeningBuilder() or explicit entity creation in tests.
     */
    public static Screening createPastScreening() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(18).withMinute(0);
        return Screening.builder()
            .movie(createDefaultMovie())
            .hall(createDefaultHall())
            .startTime(yesterday)
            .endTime(yesterday.plusHours(2).plusMinutes(30))
            .basePrice(25.0)
            .active(true)
            .bookings(new HashSet<>())
            .createdAt(LocalDateTime.now().minusDays(10))
            .updatedAt(LocalDateTime.now().minusDays(10))
            .build();
    }

    // ========== TicketType Fixtures ==========
    
    public static TicketType createNormalTicket() {
        return TicketType.builder()
            .name("Normal")
            .description("Standard ticket")
            .priceModifier(1.0)
            .active(true)
            .bookingSeats(new HashSet<>())
            .build();
    }

    public static TicketType createStudentTicket() {
        return TicketType.builder()
            .name("Student")
            .description("Student discount")
            .priceModifier(0.7)
            .active(true)
            .bookingSeats(new HashSet<>())
            .build();
    }

    public static TicketType createSeniorTicket() {
        return TicketType.builder()
            .name("Senior")
            .description("Senior discount")
            .priceModifier(0.75)
            .active(true)
            .bookingSeats(new HashSet<>())
            .build();
    }

    // ========== Booking Fixtures ==========
    
    public static Booking createPendingBooking() {
        return Booking.builder()
            .bookingNumber(UUID.randomUUID().toString())
            .user(createDefaultUser())
            .screening(createUpcomingScreening())
            .bookingSeats(new HashSet<>())
            .totalPrice(50.0)
            .status(Booking.BookingStatus.PENDING)
            .customerEmail("test@example.com")
            .customerPhone("+48123456789")
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now().minusHours(1))
            .build();
    }

    public static Booking createConfirmedBooking() {
        return Booking.builder()
            .bookingNumber(UUID.randomUUID().toString())
            .user(createDefaultUser())
            .screening(createUpcomingScreening())
            .bookingSeats(new HashSet<>())
            .totalPrice(50.0)
            .status(Booking.BookingStatus.CONFIRMED)
            .paymentMethod("CREDIT_CARD")
            .paymentReference("PAY-123456")
            .customerEmail("test@example.com")
            .customerPhone("+48123456789")
            .createdAt(LocalDateTime.now().minusHours(2))
            .updatedAt(LocalDateTime.now().minusMinutes(30))
            .build();
    }

    public static Booking createCancelledBooking() {
        return Booking.builder()
            .bookingNumber(UUID.randomUUID().toString())
            .user(createDefaultUser())
            .screening(createUpcomingScreening())
            .bookingSeats(new HashSet<>())
            .totalPrice(50.0)
            .status(Booking.BookingStatus.CANCELLED)
            .customerEmail("test@example.com")
            .customerPhone("+48123456789")
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now().minusHours(12))
            .build();
    }

    // ========== BookingSeat Fixtures ==========
    
    public static BookingSeat createBookingSeat() {
        return BookingSeat.builder()
            .booking(createPendingBooking())
            .seat(createStandardSeat())
            .ticketType(createNormalTicket())
            .price(25.0)
            .build();
    }

    // ========== Builder Methods for Customization ==========
    
    public static User.UserBuilder userBuilder() {
        return User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("$2a$10$encoded.password.hash")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+48123456789")
            .enabled(true)
            .roles(new HashSet<>())
            .bookings(new HashSet<>());
    }

    public static Movie.MovieBuilder movieBuilder() {
        return Movie.builder()
            .title("Test Movie")
            .description("Test description")
            .genre("Action")
            .ageRating("PG-13")
            .durationMinutes(120)
            .director("Test Director")
            .cast("Actor 1, Actor 2")
            .releaseYear(2024)
            .active(true)
            .images(new HashSet<>())
            .screenings(new HashSet<>());
    }

    public static Screening.ScreeningBuilder screeningBuilder() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        return Screening.builder()
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .bookings(new HashSet<>());
    }

    /**
     * Creates a screening builder with already persisted movie and hall.
     * Use this with TestEntityManager to avoid transient entity issues.
     * Example: screeningBuilderWithPersistedDeps(entityManager).build()
     */
    public static Screening.ScreeningBuilder screeningBuilderWithPersistedDeps(org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager) {
        var movie = entityManager.persist(createDefaultMovie());
        var hall = entityManager.persist(createDefaultHall());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        return Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .bookings(new HashSet<>());
    }

    public static Booking.BookingBuilder bookingBuilder() {
        return Booking.builder()
            .bookingNumber(UUID.randomUUID().toString())
            .user(createDefaultUser())
            .screening(createUpcomingScreening())
            .bookingSeats(new HashSet<>())
            .totalPrice(0.0)
            .status(Booking.BookingStatus.PENDING)
            .customerEmail("test@example.com")
            .customerPhone("+48123456789");
    }
}
