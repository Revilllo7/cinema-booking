package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Booking;
import com.cinema.entity.BookingSeat;
import com.cinema.entity.Hall;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.TicketType;
import com.cinema.entity.User;
import com.cinema.fixtures.EntityFixtures;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BookingSeatRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for booking seats.
 */
@DataJpaTest
class BookingSeatRepositoryTest extends PostgresTestContainer {

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidBookingSeat_PersistsToDatabase() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        BookingSeat bookingSeat = BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .build();

        // When
        BookingSeat saved = bookingSeatRepository.save(bookingSeat);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingBookingSeat_ReturnsBookingSeat() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        BookingSeat bookingSeat = entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .build());
        entityManager.flush();

        // When
        Optional<BookingSeat> found = bookingSeatRepository.findById(bookingSeat.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bookingSeat.getId());
    }

    @Test
    void findById_NonExistingBookingSeat_ReturnsEmpty() {
        // When
        Optional<BookingSeat> found = bookingSeatRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByBookingId_WithMultipleSeats_ReturnsAllSeatsForBooking() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());

        Seat seat1 = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        Seat seat2 = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(6)
            .seatNumber(11)
            .seatType(Seat.SeatType.VIP)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat1)
            .ticketType(ticketType)
            .price(25.0)
            .build());
        entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat2)
            .ticketType(ticketType)
            .price(35.0)
            .build());
        entityManager.flush();

        // When
        List<BookingSeat> result = bookingSeatRepository.findByBookingId(booking.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bs -> bs.getBooking().getId().equals(booking.getId()));
    }

    @Test
    void findByBookingId_NoSeats_ReturnsEmptyList() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        entityManager.flush();

        // When
        List<BookingSeat> result = bookingSeatRepository.findByBookingId(booking.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findBySeatIdAndScreeningId_WithBookingSeat_ReturnsBookingSeat() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());

        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .build());
        entityManager.flush();

        // When
        List<BookingSeat> result = bookingSeatRepository.findBySeatIdAndScreeningId(seat.getId(), screening.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeat().getId()).isEqualTo(seat.getId());
    }

    @Test
    void findOccupiedSeatsByScreeningId_WithOccupiedSeats_ReturnsOccupiedSeats() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());

        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .seatStatus(BookingSeat.SeatStatus.OCCUPIED)
            .build());
        entityManager.flush();

        // When
        List<BookingSeat> result = bookingSeatRepository.findOccupiedSeatsByScreeningId(screening.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatStatus()).isEqualTo(BookingSeat.SeatStatus.OCCUPIED);
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingBookingSeat_PersistsChanges() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hall)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        BookingSeat bookingSeat = entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .seatStatus(BookingSeat.SeatStatus.RESERVED)
            .build());
        entityManager.flush();
        entityManager.clear();

        // When
        BookingSeat toUpdate = bookingSeatRepository.findById(bookingSeat.getId()).orElseThrow();
        toUpdate.setSeatStatus(BookingSeat.SeatStatus.OCCUPIED);
        BookingSeat updated = bookingSeatRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getSeatStatus()).isEqualTo(BookingSeat.SeatStatus.OCCUPIED);
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingBookingSeat_RemovesFromDatabase() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        var hallForSeat = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hallForSeat)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        BookingSeat bookingSeat = entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .build());
        entityManager.flush();
        Long bookingSeatId = bookingSeat.getId();

        // When
        bookingSeatRepository.deleteById(bookingSeatId);
        entityManager.flush();

        // Then
        assertThat(bookingSeatRepository.findById(bookingSeatId)).isEmpty();
    }

    @Test
    void existsById_ExistingBookingSeat_ReturnsTrue() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        var hallForSeat = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        Seat seat = entityManager.persist(Seat.builder()
            .hall(hallForSeat)
            .rowNumber(5)
            .seatNumber(10)
            .seatType(Seat.SeatType.STANDARD)
            .active(true)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        BookingSeat bookingSeat = entityManager.persist(BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(25.0)
            .build());
        entityManager.flush();

        // When
        boolean exists = bookingSeatRepository.existsById(bookingSeat.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingBookingSeat_ReturnsFalse() {
        // When
        boolean exists = bookingSeatRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        var movie = entityManager.persist(EntityFixtures.createDefaultMovie());
        var hall = entityManager.persist(Hall.builder()
            .name("Hall-" + System.nanoTime())
            .totalSeats(100)
            .rowsCount(10)
            .seatsPerRow(10)
            .active(true)
            .build());
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        Screening screening = entityManager.persist(Screening.builder()
            .movie(movie)
            .hall(hall)
            .startTime(tomorrow)
            .endTime(tomorrow.plusHours(2))
            .basePrice(25.0)
            .active(true)
            .build());
        Booking booking = entityManager.persist(EntityFixtures.bookingBuilder()
            .user(user)
            .screening(screening)
            .build());
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());

        for (int i = 0; i < 12; i++) {
            Seat seat = entityManager.persist(Seat.builder()
                .hall(hall)
                .rowNumber(i / 5)
                .seatNumber(i % 5)
                .seatType(Seat.SeatType.STANDARD)
                .active(true)
                .build());
            entityManager.persist(BookingSeat.builder()
                .booking(booking)
                .seat(seat)
                .ticketType(ticketType)
                .price(25.0)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<BookingSeat> page = bookingSeatRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }
}
