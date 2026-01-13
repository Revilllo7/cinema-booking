package com.cinema.service;

import com.cinema.cart.CartItem;
import com.cinema.cart.SessionCart;
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
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final SessionCart sessionCart;
    private final CartService cartService;
    private final BookingRepository bookingRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatLockRepository seatLockRepository;
    private final UserRepository userRepository;
    private final SeatReservationService seatReservationService;
    private final QrCodeService qrCodeService;

    @Transactional
    public CheckoutResponse finalizeCheckout(Long screeningId,
                                              CheckoutRequest request,
                                              String sessionId,
                                              String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Login required to complete checkout");
        }

        CartResponse snapshot = cartService.getCart(screeningId, sessionId, username);
        if (snapshot.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot finalize checkout with an empty cart");
        }
        if (sessionCart.getScreeningId() == null || !sessionCart.getScreeningId().equals(screeningId)) {
            throw new IllegalStateException("Cart is bound to another screening");
        }

        Screening screening = screeningRepository.findById(screeningId)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Map<Long, SeatLock> activeLocks = loadLocksForOwner(screeningId, sessionId, username);
        validateLocks(activeLocks.keySet(), sessionCart.getItems().keySet());

        Booking booking = Booking.builder()
            .user(user)
            .screening(screening)
            .customerEmail(resolveCustomerEmail(request, user))
            .customerPhone(request.customerPhone())
            .paymentMethod(request.paymentMethod())
            .paymentReference(buildPaymentReference())
            .totalPrice(snapshot.getSubtotal())
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        sessionCart.getItems().values().forEach(item -> booking.getBookingSeats().add(buildBookingSeat(item, booking)));

        Booking savedBooking = bookingRepository.save(booking);
        seatReservationService.releaseAll(screeningId, sessionId);
        seatReservationService.broadcastSeatMap(screeningId);
        sessionCart.clear();

        return CheckoutResponse.builder()
            .bookingNumber(savedBooking.getBookingNumber())
            .paymentReference(savedBooking.getPaymentReference())
            .totalPrice(savedBooking.getTotalPrice())
            .items(snapshot.getItems())
            .qrCodeImage(qrCodeService.generateBookingCode(savedBooking.getBookingNumber()))
            .build();
    }

    private void validateLocks(Set<Long> locks, Set<Long> seatsInCart) {
        if (locks.size() != seatsInCart.size()) {
            throw new IllegalStateException("One or more seats are no longer locked");
        }
        seatsInCart.forEach(seatId -> {
            if (!locks.contains(seatId)) {
                throw new IllegalStateException("Seat " + seatId + " is no longer locked");
            }
        });
    }

    private BookingSeat buildBookingSeat(CartItem cartItem, Booking booking) {
        Seat seat = seatRepository.findById(cartItem.getSeatId())
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", cartItem.getSeatId()));
        TicketType ticketType = ticketTypeRepository.findById(cartItem.getTicketTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", cartItem.getTicketTypeId()));

        return BookingSeat.builder()
            .booking(booking)
            .seat(seat)
            .ticketType(ticketType)
            .price(cartItem.getPrice())
            .seatStatus(BookingSeat.SeatStatus.OCCUPIED)
            .build();
    }

    private String resolveCustomerEmail(CheckoutRequest request, User user) {
        if (request.customerEmail() != null && !request.customerEmail().isBlank()) {
            return request.customerEmail();
        }
        return user.getEmail();
    }

    private String buildPaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Map<Long, SeatLock> loadLocksForOwner(Long screeningId, String sessionId, String username) {
        Map<Long, SeatLock> locks = seatLockRepository.findActiveLocksForSession(screeningId, sessionId, LocalDateTime.now()).stream()
            .collect(Collectors.toMap(lock -> lock.getSeat().getId(), Function.identity()));
        if (username != null && !username.isBlank()) {
            List<SeatLock> userLocks = seatLockRepository.findActiveLocksForUsername(screeningId, username, LocalDateTime.now());
            if (userLocks != null) {
                userLocks.forEach(lock -> locks.putIfAbsent(lock.getSeat().getId(), lock));
            }
        }
        return locks;
    }
}
