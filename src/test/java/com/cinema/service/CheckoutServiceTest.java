package com.cinema.service;

import com.cinema.cart.CartItem;
import com.cinema.cart.SessionCart;
import com.cinema.dto.CartItemResponse;
import com.cinema.dto.CartResponse;
import com.cinema.dto.CheckoutRequest;
import com.cinema.dto.CheckoutResponse;
import com.cinema.entity.Booking;
import com.cinema.entity.BookingSeat;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.SeatLock;
import com.cinema.entity.TicketType;
import com.cinema.entity.User;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import com.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final Long SCREENING_ID = 5L;
    private static final String SESSION_ID = "mock-session";
    private static final String USERNAME = "testuser";

    @Mock
    private CartService cartService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private SeatLockRepository seatLockRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private QrCodeService qrCodeService;

    @Spy
    private SessionCart sessionCart = new SessionCart();

    @InjectMocks
    private CheckoutService checkoutService;

    private Screening screening;
    private Seat seat;
    private TicketType ticketType;
    private User user;
    private CheckoutRequest checkoutRequest;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        screening = EntityFixtures.createUpcomingScreening();
        screening.setId(SCREENING_ID);
        screening.getHall().setId(10L);

        seat = EntityFixtures.createStandardSeat();
        seat.setId(101L);
        seat.getHall().setId(10L);

        ticketType = TicketType.builder()
            .id(7L)
            .name("STANDARD")
            .priceModifier(25.0)
            .active(true)
            .build();

        user = EntityFixtures.createDefaultUser();
        user.setId(42L);

        checkoutRequest = new CheckoutRequest("buyer@example.com", "+48123456789", "John Doe", "CARD");

        CartItemResponse itemResponse = CartItemResponse.builder()
            .seatId(seat.getId())
            .rowNumber(seat.getRowNumber())
            .seatNumber(seat.getSeatNumber())
            .ticketType(ticketTypeName())
            .ticketTypeId(ticketType.getId())
            .price(ticketType.getPriceModifier())
            .lockExpiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
        cartResponse = CartResponse.builder()
            .screeningId(SCREENING_ID)
            .items(List.of(itemResponse))
            .subtotal(ticketType.getPriceModifier())
            .build();

        sessionCart.setScreeningId(SCREENING_ID);
        sessionCart.getItems().put(seat.getId(), CartItem.builder()
            .seatId(seat.getId())
            .rowNumber(seat.getRowNumber())
            .seatNumber(seat.getSeatNumber())
            .ticketType(ticketTypeName())
            .ticketTypeId(ticketType.getId())
            .price(ticketType.getPriceModifier())
            .lockExpiresAt(LocalDateTime.now().plusMinutes(5))
            .build());
    }

    @Test
    void finalizeCheckout_WithValidCart_PersistsBookingAndReturnsQr() {
        stubCartAndEntities();
        stubSeatLocks();

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        given(bookingRepository.save(bookingCaptor.capture())).willAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(200L);
            saved.setBookingNumber("BOOK-001");
            saved.setPaymentReference("PAY-XYZ");
            return saved;
        });
        given(qrCodeService.generateBookingCode("BOOK-001")).willReturn("qr-base64");

        // When
        CheckoutResponse response = checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME);

        // Then
        assertThat(response.getBookingNumber()).isEqualTo("BOOK-001");
        assertThat(response.getQrCodeImage()).isEqualTo("qr-base64");
        assertThat(sessionCart.getItems()).isEmpty();
        assertThat(response.getItems()).hasSize(1);

        Booking persisted = bookingCaptor.getValue();
        assertThat(persisted.getBookingSeats()).hasSize(1);
        BookingSeat reservedSeat = persisted.getBookingSeats().iterator().next();
        assertThat(reservedSeat.getSeatStatus()).isEqualTo(BookingSeat.SeatStatus.OCCUPIED);
        assertThat(reservedSeat.getTicketType().getId()).isEqualTo(ticketType.getId());

        then(seatReservationService).should().releaseAll(SCREENING_ID, SESSION_ID);
        then(seatReservationService).should().broadcastSeatMap(SCREENING_ID);
    }

    @Test
    void finalizeCheckout_WhenUserMissing_ThrowsIllegalStateException() {
        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Login required");
    }

    @Test
    void finalizeCheckout_WhenCartEmpty_ThrowsIllegalStateException() {
        given(cartService.getCart(SCREENING_ID, SESSION_ID, USERNAME)).willReturn(CartResponse.builder()
            .screeningId(SCREENING_ID)
            .items(List.of())
            .subtotal(0.0)
            .build());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("empty cart");
    }

    @Test
    void finalizeCheckout_WhenCartBoundToDifferentScreening_ThrowsIllegalStateException() {
        sessionCart.setScreeningId(999L);
        given(cartService.getCart(SCREENING_ID, SESSION_ID, USERNAME)).willReturn(cartResponse);

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("another screening");
    }

    @Test
    void finalizeCheckout_WhenScreeningMissing_ThrowsResourceNotFoundException() {
        given(cartService.getCart(SCREENING_ID, SESSION_ID, USERNAME)).willReturn(cartResponse);
        given(screeningRepository.findById(SCREENING_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Screening");
    }

    @Test
    void finalizeCheckout_WhenUserNotFound_ThrowsResourceNotFoundException() {
        stubCartAndEntities();
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    @Test
    void finalizeCheckout_WhenSeatLocksMissing_ThrowsIllegalStateException() {
        stubCartAndEntities();
        given(seatLockRepository.findActiveLocksForSession(eq(SCREENING_ID), eq(SESSION_ID), any(LocalDateTime.class)))
            .willReturn(List.of());
        given(seatLockRepository.findActiveLocksForUsername(eq(SCREENING_ID), eq(USERNAME), any(LocalDateTime.class)))
            .willReturn(List.of());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no longer locked");
    }

    @Test
    void finalizeCheckout_WhenSeatNotFound_ThrowsResourceNotFoundException() {
        stubCartAndEntities();
        given(seatLockRepository.findActiveLocksForSession(eq(SCREENING_ID), eq(SESSION_ID), any(LocalDateTime.class)))
            .willReturn(List.of(activeSeatLock()));
        given(seatLockRepository.findActiveLocksForUsername(eq(SCREENING_ID), eq(USERNAME), any(LocalDateTime.class)))
            .willReturn(List.of());
        given(seatRepository.findById(seat.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Seat");
    }

    @Test
    void finalizeCheckout_WhenTicketTypeMissing_ThrowsResourceNotFoundException() {
        stubCartAndEntities();
        given(seatLockRepository.findActiveLocksForSession(eq(SCREENING_ID), eq(SESSION_ID), any(LocalDateTime.class)))
            .willReturn(List.of(activeSeatLock()));
        given(seatLockRepository.findActiveLocksForUsername(eq(SCREENING_ID), eq(USERNAME), any(LocalDateTime.class)))
            .willReturn(List.of());
        given(seatRepository.findById(seat.getId())).willReturn(Optional.of(seat));
        given(ticketTypeRepository.findById(ticketType.getId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> checkoutService.finalizeCheckout(SCREENING_ID, checkoutRequest, SESSION_ID, USERNAME))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("TicketType");
    }

    @Test
    void finalizeCheckout_WithBlankEmail_FallsBackToUserEmail() {
        stubCartAndEntities();
        stubSeatLocks();
        CheckoutRequest blankEmailRequest = new CheckoutRequest(" ", checkoutRequest.customerPhone(), checkoutRequest.cardholderName(), checkoutRequest.paymentMethod());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        given(bookingRepository.save(bookingCaptor.capture())).willAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(201L);
            saved.setBookingNumber("BOOK-EMAIL");
            saved.setPaymentReference("PAY-EMAIL");
            return saved;
        });
        given(qrCodeService.generateBookingCode("BOOK-EMAIL")).willReturn("qr-email");

        checkoutService.finalizeCheckout(SCREENING_ID, blankEmailRequest, SESSION_ID, USERNAME);

        Booking persisted = bookingCaptor.getValue();
        assertThat(persisted.getCustomerEmail()).isEqualTo(user.getEmail());
    }

    private com.cinema.entity.TicketTypeName ticketTypeName() {
        return com.cinema.entity.TicketTypeName.STANDARD;
    }

    private void stubCartAndEntities() {
        given(cartService.getCart(SCREENING_ID, SESSION_ID, USERNAME)).willReturn(cartResponse);
        given(screeningRepository.findById(SCREENING_ID)).willReturn(Optional.of(screening));
        given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));
    }

    private void stubSeatLocks() {
        given(seatLockRepository.findActiveLocksForSession(eq(SCREENING_ID), eq(SESSION_ID), any(LocalDateTime.class)))
            .willReturn(List.of(activeSeatLock()));
        given(seatLockRepository.findActiveLocksForUsername(eq(SCREENING_ID), eq(USERNAME), any(LocalDateTime.class)))
            .willReturn(List.of());
        given(seatRepository.findById(seat.getId())).willReturn(Optional.of(seat));
        given(ticketTypeRepository.findById(ticketType.getId())).willReturn(Optional.of(ticketType));
    }

    private SeatLock activeSeatLock() {
        return SeatLock.builder()
            .seat(seat)
            .screening(screening)
            .sessionId(SESSION_ID)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
    }
}
