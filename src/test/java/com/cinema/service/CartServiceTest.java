package com.cinema.service;

import com.cinema.cart.CartItem;
import com.cinema.cart.SessionCart;
import com.cinema.dto.CartResponse;
import com.cinema.dto.TicketOptionResponse;
import com.cinema.entity.Hall;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.SeatLock;
import com.cinema.entity.TicketType;
import com.cinema.entity.TicketTypeName;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private SeatLockRepository seatLockRepository;
    @Mock
    private SeatReservationService seatReservationService;

    private SessionCart sessionCart;
    private CartService cartService;

    private Screening screening;
    private Seat seat;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        sessionCart = new SessionCart();
        cartService = new CartService(sessionCart, screeningRepository, seatRepository,
            ticketTypeRepository, seatLockRepository, seatReservationService);

        Hall hall = Hall.builder().id(2L).build();
        screening = Screening.builder().id(5L).hall(hall).build();
        seat = Seat.builder().id(11L).hall(hall).rowNumber(1).seatNumber(1).build();
        ticketType = TicketType.builder().id(3L).name("Standard").priceModifier(35.0).active(true).build();
    }

    @Test
    void addSeat_WhenLocked_AddsItemToCart() {
        SeatLock lock = SeatLock.builder()
            .seat(seat)
            .screening(screening)
            .sessionId("sess")
            .status(SeatLock.SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();

        when(screeningRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(11L)).thenReturn(Optional.of(seat));
        when(ticketTypeRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(ticketType));
        when(seatLockRepository.findActiveLockForSession(eq(5L), eq(11L), eq("sess"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(lock));

        CartResponse response = cartService.addSeat(5L, 11L, 3L, "sess", "jane");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getSubtotal()).isEqualTo(35.0);
        assertThat(sessionCart.getScreeningId()).isEqualTo(5L);

        verify(screeningRepository).findByIdAndActiveTrue(5L);
        verify(seatRepository).findById(11L);
        verify(ticketTypeRepository).findByIdAndActiveTrue(3L);
        verify(seatLockRepository).findActiveLockForSession(eq(5L), eq(11L), eq("sess"), any(LocalDateTime.class));
    }

    @Test
    void addSeat_WhenAlreadyPresent_ThrowsException() {
        sessionCart.setScreeningId(5L);
        sessionCart.getItems().put(11L, CartItem.builder().seatId(11L).build());

        when(screeningRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(screening));
        when(seatRepository.findById(11L)).thenReturn(Optional.of(seat));
        when(ticketTypeRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(ticketType));
        when(seatLockRepository.findActiveLockForSession(eq(5L), eq(11L), eq("sess"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(SeatLock.builder()
                .seat(seat)
                .screening(screening)
                .sessionId("sess")
                .status(SeatLock.SeatLockStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build()));

        assertThatThrownBy(() -> cartService.addSeat(5L, 11L, 3L, "sess", null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Seat already present in cart");

        verify(screeningRepository).findByIdAndActiveTrue(5L);
        verify(seatRepository).findById(11L);
        verify(ticketTypeRepository).findByIdAndActiveTrue(3L);
        verify(seatLockRepository).findActiveLockForSession(eq(5L), eq(11L), eq("sess"), any(LocalDateTime.class));
    }

    @Test
    void removeSeat_WhenLastItem_ClearsCartAndReleasesLock() {
        sessionCart.setScreeningId(5L);
        sessionCart.getItems().put(11L, CartItem.builder()
            .seatId(11L)
            .ticketType(TicketTypeName.STANDARD)
            .price(20.0)
            .build());

        CartResponse response = cartService.removeSeat(5L, 11L, "sess", "jane");

        assertThat(response.getItems()).isEmpty();
        assertThat(sessionCart.getItems()).isEmpty();
        assertThat(sessionCart.getScreeningId()).isNull();
        verify(seatReservationService).releaseSeat(5L, 11L, "sess", "jane");
    }

    @Test
    void getCart_WhenLocksExpired_RemovesItems() {
        sessionCart.setScreeningId(5L);
        sessionCart.getItems().put(11L, CartItem.builder()
            .seatId(11L)
            .ticketType(TicketTypeName.STANDARD)
            .price(20.0)
            .build());

        when(seatLockRepository.findActiveLocksForSession(eq(5L), eq("sess"), any(LocalDateTime.class)))
            .thenReturn(List.of());

        CartResponse response = cartService.getCart(5L, "sess", null);

        assertThat(response.getItems()).isEmpty();
        assertThat(sessionCart.getItems()).isEmpty();
        assertThat(sessionCart.getScreeningId()).isNull();

        verify(seatLockRepository).findActiveLocksForSession(eq(5L), eq("sess"), any(LocalDateTime.class));
        verify(seatLockRepository, never()).findActiveLocksForUsername(eq(5L), anyString(), any(LocalDateTime.class));
    }

    @Test
    void getCart_WhenDifferentScreening_ReturnsEmpty() {
        sessionCart.setScreeningId(9L);
        CartResponse response = cartService.getCart(5L, "sess", null);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getScreeningId()).isEqualTo(5L);
        verifyNoMoreInteractions(seatLockRepository);
    }

    @Test
    void getTicketOptions_ReturnsMappedResponses() {
        TicketType premium = TicketType.builder()
            .id(7L)
            .name("Senior")
            .priceModifier(70.0)
            .build();
        when(ticketTypeRepository.findByActiveTrueOrderByPriceModifier())
            .thenReturn(List.of(ticketType, premium));

        List<TicketOptionResponse> responses = cartService.getTicketOptions();

        assertThat(responses)
            .hasSize(2)
            .extracting(TicketOptionResponse::getTicketTypeId)
            .containsExactly(3L, 7L);
        assertThat(responses.get(1).getName()).isEqualTo(TicketTypeName.SENIOR);

        verify(ticketTypeRepository).findByActiveTrueOrderByPriceModifier();
    }

    @Test
    void addSeat_WithValidSeat_AddsToCart() {
        when(screeningRepository.findByIdAndActiveTrue(screening.getId())).thenReturn(Optional.of(screening));
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
        when(ticketTypeRepository.findByIdAndActiveTrue(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(seatLockRepository.findActiveLockForSession(eq(screening.getId()), eq(seat.getId()), eq("session-1"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(SeatLock.builder()
                .seat(seat)
                .screening(screening)
                .sessionId("session-1")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build()));

        CartResponse response = cartService.addSeat(screening.getId(), seat.getId(), 
            ticketType.getId(), "session-1", "testuser");

        assertThat(response).isNotNull();

        verify(screeningRepository).findByIdAndActiveTrue(screening.getId());
        verify(seatRepository).findById(seat.getId());
        verify(ticketTypeRepository).findByIdAndActiveTrue(ticketType.getId());
        verify(seatLockRepository).findActiveLockForSession(eq(screening.getId()), eq(seat.getId()), eq("session-1"), any(LocalDateTime.class));
    }

    @Test
    void getTicketOptions_ReturnsAllActiveTypes() {
        TicketType senior = TicketType.builder()
            .id(2L)
            .name("Senior")
            .priceModifier(70.0)
            .active(true)
            .build();

        when(ticketTypeRepository.findByActiveTrueOrderByPriceModifier())
            .thenReturn(List.of(ticketType, senior));

        List<TicketOptionResponse> options = cartService.getTicketOptions();

        assertThat(options).hasSize(2);
        verify(ticketTypeRepository).findByActiveTrueOrderByPriceModifier();
    }

    @Test
    void addSeat_WhenSeatAlreadyInCart_Throws() {
        when(screeningRepository.findByIdAndActiveTrue(screening.getId())).thenReturn(Optional.of(screening));
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
        when(ticketTypeRepository.findByIdAndActiveTrue(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(seatLockRepository.findActiveLockForSession(eq(screening.getId()), eq(seat.getId()), eq("session-1"), any(LocalDateTime.class)))
            .thenReturn(Optional.of(SeatLock.builder()
                .seat(seat)
                .screening(screening)
                .sessionId("session-1")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build()));

        cartService.addSeat(screening.getId(), seat.getId(), ticketType.getId(), "session-1", "testuser");

        assertThatThrownBy(() -> cartService.addSeat(screening.getId(), seat.getId(), 
            ticketType.getId(), "session-1", "testuser"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getCart_WithMultipleScreenings_ReturnsCurrentScreeningCart() {
        sessionCart.setScreeningId(screening.getId());
        sessionCart.getItems().put(seat.getId(), CartItem.builder()
            .seatId(seat.getId())
            .ticketType(TicketTypeName.STANDARD)
            .price(35.0)
            .build());

        SeatLock lock = SeatLock.builder()
            .seat(seat)
            .screening(screening)
            .sessionId("session-1")
            .status(SeatLock.SeatLockStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .build();

        when(seatLockRepository.findActiveLocksForSession(eq(screening.getId()), eq("session-1"), any(LocalDateTime.class)))
            .thenReturn(List.of(lock));
        when(seatLockRepository.findActiveLocksForUsername(eq(screening.getId()), eq("testuser"), any(LocalDateTime.class)))
            .thenReturn(List.of());

        CartResponse response = cartService.getCart(screening.getId(), "session-1", "testuser");

        assertThat(response.getItems()).hasSize(1);

        verify(seatLockRepository).findActiveLocksForSession(eq(screening.getId()), eq("session-1"), any(LocalDateTime.class));
        verify(seatLockRepository).findActiveLocksForUsername(eq(screening.getId()), eq("testuser"), any(LocalDateTime.class));
    }
}
