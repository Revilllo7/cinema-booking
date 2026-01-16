package com.cinema.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityModelTest {

    @ParameterizedTest
    @MethodSource("entitiesWithSameId")
    void equalsAndHashCodeDependOnId(Object left, Object right) {
        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }

    @ParameterizedTest
    @MethodSource("entitiesWithDifferentId")
    void entitiesWithDifferentIdsAreNotEqual(Object left, Object right) {
        assertThat(left).isNotEqualTo(right);
    }

    @Test
    void builderDefaultsAreApplied() {
        TicketType ticketType = TicketType.builder().id(1L).name("Standard").priceModifier(20.0).build();
        Movie movie = Movie.builder().id(2L).title("Inception").build();
        Seat seat = Seat.builder().id(3L).hall(Hall.builder().id(9L).build()).rowNumber(1).seatNumber(1).build();
        Booking booking = Booking.builder().id(4L).build();
        BookingSeat bookingSeat = BookingSeat.builder().id(5L).build();

        assertThat(ticketType.getActive()).isTrue();
        assertThat(movie.getActive()).isTrue();
        assertThat(seat.getSeatType()).isEqualTo(Seat.SeatType.STANDARD);
        assertThat(seat.getActive()).isTrue();
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.PENDING);
        assertThat(bookingSeat.getSeatStatus()).isEqualTo(BookingSeat.SeatStatus.RESERVED);
    }

    @Test
    void ticketTypeNameFromString_IsCaseInsensitive() {
        assertThat(TicketTypeName.fromString("standard")).isEqualTo(TicketTypeName.STANDARD);
        assertThat(TicketTypeName.fromString("Student")).isEqualTo(TicketTypeName.STUDENT);
    }

    @Test
    void ticketTypeNameFromString_InvalidValueThrows() {
        assertThatThrownBy(() -> TicketTypeName.fromString("Unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported ticket type");
    }

    private static Stream<Arguments> entitiesWithSameId() {
        return Stream.of(
            Arguments.of(ticketType(1L), ticketType(1L)),
            Arguments.of(movie(2L), movie(2L)),
            Arguments.of(screening(3L), screening(3L)),
            Arguments.of(booking(4L), booking(4L)),
            Arguments.of(hall(5L), hall(5L)),
            Arguments.of(seat(6L), seat(6L)),
            Arguments.of(bookingSeat(7L), bookingSeat(7L))
        );
    }

    private static Stream<Arguments> entitiesWithDifferentId() {
        return Stream.of(
            Arguments.of(ticketType(1L), ticketType(2L)),
            Arguments.of(movie(2L), movie(3L)),
            Arguments.of(screening(3L), screening(4L)),
            Arguments.of(booking(4L), booking(5L))
        );
    }

    private static TicketType ticketType(Long id) {
        return TicketType.builder().id(id).name("Standard").priceModifier(20.0).build();
    }

    private static Movie movie(Long id) {
        return Movie.builder().id(id).title("Movie").build();
    }

    private static Screening screening(Long id) {
        return Screening.builder().id(id).movie(movie(1L)).hall(hall(1L)).startTime(java.time.LocalDateTime.now()).endTime(java.time.LocalDateTime.now().plusHours(2)).basePrice(25.0).build();
    }

    private static Booking booking(Long id) {
        return Booking.builder().id(id).build();
    }

    private static Hall hall(Long id) {
        return Hall.builder().id(id).name("Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).build();
    }

    private static Seat seat(Long id) {
        return Seat.builder().id(id).hall(hall(1L)).rowNumber(1).seatNumber(1).build();
    }

    private static BookingSeat bookingSeat(Long id) {
        return BookingSeat.builder().id(id).build();
    }
}
