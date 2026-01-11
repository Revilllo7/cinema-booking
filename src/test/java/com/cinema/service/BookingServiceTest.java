package com.cinema.service;

import com.cinema.dto.BookingDTO;
import com.cinema.entity.Booking;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.TicketType;
import com.cinema.entity.User;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.DTOFixtures;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import com.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Unit tests for BookingService using Mockito.
 * Tests booking management business logic in isolation from database.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;
    private User testUser;
    private Screening testScreening;
    private Seat testSeat;
    private TicketType testTicketType;

    @BeforeEach
    void setUp() {
        testUser = EntityFixtures.createDefaultUser();
        testUser.setId(1L);

        testScreening = EntityFixtures.createUpcomingScreening();
        testScreening.setId(1L);

        testSeat = EntityFixtures.createStandardSeat();
        testSeat.setId(1L);

        testTicketType = EntityFixtures.createNormalTicket();
        testTicketType.setId(1L);

        testBooking = EntityFixtures.createPendingBooking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setScreening(testScreening);
    }

    // ========== getAllBookings Tests ==========

    @Test
    void getAllBookings_WithExistingBookings_ReturnsPaginatedBookingDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
        given(bookingRepository.findAll(pageable)).willReturn(bookingPage);

        // When
        Page<BookingDTO> result = bookingService.getAllBookings(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllBookings_CallsRepository_ExactlyOnce() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
        given(bookingRepository.findAll(pageable)).willReturn(bookingPage);

        // When
        bookingService.getAllBookings(pageable);

        // Then
        then(bookingRepository).should(times(1)).findAll(pageable);
    }

    // ========== getBookingById Tests ==========

    @Test
    void getBookingById_ExistingBooking_ReturnsBookingDTO() {
        // Given
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));

        // When
        BookingDTO result = bookingService.getBookingById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_ExistingBooking_MapsAllFields() {
        // Given
        given(bookingRepository.findById(1L)).willReturn(Optional.of(testBooking));

        // When
        BookingDTO result = bookingService.getBookingById(1L);

        // Then
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
        assertThat(result.getScreeningId()).isEqualTo(testScreening.getId());
    }

    @Test
    void getBookingById_NonExistingBooking_ThrowsResourceNotFoundException() {
        // Given
        given(bookingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Booking");
    }

    // ========== getBookingByBookingNumber Tests ==========

    @Test
    void getBookingByBookingNumber_ExistingNumber_ReturnsBookingDTO() {
        // Given
        String bookingNumber = "BK123456";
        given(bookingRepository.findByBookingNumber(bookingNumber))
            .willReturn(Optional.of(testBooking));

        // When
        BookingDTO result = bookingService.getBookingByBookingNumber(bookingNumber);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingByBookingNumber_NonExistingNumber_ThrowsResourceNotFoundException() {
        // Given
        String nonExistingNumber = "BK999999";
        given(bookingRepository.findByBookingNumber(nonExistingNumber))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingByBookingNumber(nonExistingNumber))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("bookingNumber");
    }

    // ========== getBookingsByUser Tests ==========

    @Test
    void getBookingsByUser_ExistingUser_ReturnsUserBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
        given(userRepository.existsById(1L)).willReturn(true);
        given(bookingRepository.findByUserId(1L, pageable)).willReturn(bookingPage);

        // When
        Page<BookingDTO> result = bookingService.getBookingsByUser(1L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getBookingsByUser_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(userRepository.existsById(999L)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookingService.getBookingsByUser(999L, pageable))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    @Test
    void getBookingsByUser_ExistingUser_VerifiesUserExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
        given(userRepository.existsById(1L)).willReturn(true);
        given(bookingRepository.findByUserId(1L, pageable)).willReturn(bookingPage);

        // When
        bookingService.getBookingsByUser(1L, pageable);

        // Then
        then(userRepository).should(times(1)).existsById(1L);
    }

    // ========== getBookingsByScreening Tests ==========

    @Test
    void getBookingsByScreening_ExistingScreening_ReturnsScreeningBookings() {
        // Given
        List<Booking> bookings = List.of(testBooking);
        given(bookingRepository.findByScreeningId(1L)).willReturn(bookings);

        // When
        List<BookingDTO> result = bookingService.getBookingsByScreening(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByScreening_NonExistingScreening_ReturnsEmptyList() {
        // Given
        given(bookingRepository.findByScreeningId(999L)).willReturn(List.of());

        // When
        List<BookingDTO> result = bookingService.getBookingsByScreening(999L);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== getBookingsByStatus Tests ==========

    @Test
    void getBookingsByStatus_PendingStatus_ReturnsPendingBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));
        given(bookingRepository.findByStatus(Booking.BookingStatus.PENDING, pageable))
            .willReturn(bookingPage);

        // When
        Page<BookingDTO> result = bookingService.getBookingsByStatus(Booking.BookingStatus.PENDING, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getBookingsByStatus_ConfirmedStatus_ReturnsConfirmedBookings() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> emptyPage = Page.empty();
        given(bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED, pageable))
            .willReturn(emptyPage);

        // When
        Page<BookingDTO> result = bookingService.getBookingsByStatus(Booking.BookingStatus.CONFIRMED, pageable);

        // Then
        assertThat(result.getTotalElements()).isZero();
    }

    // ========== getBookingsByDateRange Tests ==========

    @Test
    void getBookingsByDateRange_ValidRange_ReturnsBookingsInRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 31, 23, 59);
        List<Booking> bookings = List.of(testBooking);
        given(bookingRepository.findByCreatedAtBetween(startDate, endDate))
            .willReturn(bookings);

        // When
        List<BookingDTO> result = bookingService.getBookingsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByDateRange_EmptyRange_ReturnsEmptyList() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 1, 1, 0);
        given(bookingRepository.findByCreatedAtBetween(startDate, endDate))
            .willReturn(List.of());

        // When
        List<BookingDTO> result = bookingService.getBookingsByDateRange(startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    // ========== createBooking Tests ==========

    @Test
    void createBooking_ValidBooking_ReturnsCreatedBookingDTO() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        Booking savedBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .user(testUser)
            .screening(testScreening)
            .status(Booking.BookingStatus.PENDING)
            .build();

        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));
        given(seatRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testSeat));
        given(ticketTypeRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testTicketType));
        given(bookingRepository.save(any(Booking.class)))
            .willReturn(savedBooking);

        // When
        BookingDTO result = bookingService.createBooking(newBookingDTO);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createBooking_ValidBooking_SetsPendingStatus() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        Booking savedBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .user(testUser)
            .screening(testScreening)
            .status(Booking.BookingStatus.PENDING)
            .build();

        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));
        given(seatRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testSeat));
        given(ticketTypeRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testTicketType));
        given(bookingRepository.save(any(Booking.class)))
            .willReturn(savedBooking);

        // When
        BookingDTO result = bookingService.createBooking(newBookingDTO);

        // Then
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void createBooking_NonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(newBookingDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    @Test
    void createBooking_NonExistentScreening_ThrowsResourceNotFoundException() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(newBookingDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Screening");
    }

    @Test
    void createBooking_NoSeatsSelected_ThrowsIllegalArgumentException() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        newBookingDTO.setSeats(List.of()); // Empty seats

        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(newBookingDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("At least one seat");
    }

    @Test
    void createBooking_NonExistentSeat_ThrowsResourceNotFoundException() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));
        given(seatRepository.findById(any(Long.class)))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(newBookingDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Seat");
    }

    @Test
    void createBooking_NonExistentTicketType_ThrowsResourceNotFoundException() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));
        given(seatRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testSeat));
        given(ticketTypeRepository.findById(any(Long.class)))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(newBookingDTO))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("TicketType");
    }

    @Test
    void createBooking_ValidBooking_CallsRepositorySave() {
        // Given
        BookingDTO newBookingDTO = DTOFixtures.createBookingDTOWithoutId();
        Booking savedBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(userRepository.findById(newBookingDTO.getUserId()))
            .willReturn(Optional.of(testUser));
        given(screeningRepository.findById(newBookingDTO.getScreeningId()))
            .willReturn(Optional.of(testScreening));
        given(seatRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testSeat));
        given(ticketTypeRepository.findById(any(Long.class)))
            .willReturn(Optional.of(testTicketType));
        given(bookingRepository.save(any(Booking.class)))
            .willReturn(savedBooking);

        // When
        bookingService.createBooking(newBookingDTO);

        // Then
        then(bookingRepository).should(times(1)).save(any(Booking.class));
    }

    // ========== confirmBooking Tests ==========

    @Test
    void confirmBooking_PendingBooking_ReturnsConfirmedBookingDTO() {
        // Given
        Booking pendingBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.PENDING)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(pendingBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(pendingBooking);

        // When
        BookingDTO result = bookingService.confirmBooking(1L, "CREDIT_CARD", "TXN123456");

        // Then
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void confirmBooking_NonExistingBooking_ThrowsResourceNotFoundException() {
        // Given
        given(bookingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.confirmBooking(999L, "CREDIT_CARD", "TXN123456"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmBooking_NonPendingBooking_ThrowsIllegalStateException() {
        // Given
        Booking confirmedBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.CONFIRMED)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(confirmedBooking));

        // When & Then
        assertThatThrownBy(() -> bookingService.confirmBooking(1L, "CREDIT_CARD", "TXN123456"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only pending bookings");
    }

    @Test
    void confirmBooking_ValidBooking_StoresPaymentDetails() {
        // Given
        Booking pendingBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.PENDING)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(pendingBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(pendingBooking);

        // When
        bookingService.confirmBooking(1L, "CREDIT_CARD", "TXN123456");

        // Then
        then(bookingRepository).should(times(1)).save(any(Booking.class));
    }

    // ========== cancelBooking Tests ==========

    @Test
    void cancelBooking_PendingBooking_ReturnsCancelledBookingDTO() {
        // Given
        Booking pendingBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.PENDING)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(pendingBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(pendingBooking);

        // When
        BookingDTO result = bookingService.cancelBooking(1L, "User request");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelBooking_AlreadyCancelledBooking_ThrowsIllegalStateException() {
        // Given
        Booking cancelledBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.CANCELLED)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(cancelledBooking));

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(1L, "User request"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already cancelled");
    }

    @Test
    void cancelBooking_NonExistingBooking_ThrowsResourceNotFoundException() {
        // Given
        given(bookingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(999L, "User request"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelBooking_ValidBooking_CallsRepositorySave() {
        // Given
        Booking pendingBooking = EntityFixtures.bookingBuilder()
            .id(1L)
            .status(Booking.BookingStatus.PENDING)
            .user(testUser)
            .screening(testScreening)
            .build();

        given(bookingRepository.findById(1L)).willReturn(Optional.of(pendingBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(pendingBooking);

        // When
        bookingService.cancelBooking(1L, "User request");

        // Then
        then(bookingRepository).should(times(1)).save(any(Booking.class));
    }

    // ========== deleteBooking Tests ==========

    @Test
    void deleteBooking_ExistingBooking_CallsRepositoryDelete() {
        // Given
        given(bookingRepository.existsById(1L)).willReturn(true);

        // When
        bookingService.deleteBooking(1L);

        // Then
        then(bookingRepository).should(times(1)).deleteById(1L);
    }

    @Test
    void deleteBooking_NonExistingBooking_ThrowsResourceNotFoundException() {
        // Given
        given(bookingRepository.existsById(999L)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> bookingService.deleteBooking(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteBooking_NonExistingBooking_DoesNotCallRepositoryDelete() {
        // Given
        given(bookingRepository.existsById(999L)).willReturn(false);

        // When & Then
        try {
            bookingService.deleteBooking(999L);
        } catch (ResourceNotFoundException e) {
            // Expected
        }
        then(bookingRepository).should(never()).deleteById(any());
    }
}
