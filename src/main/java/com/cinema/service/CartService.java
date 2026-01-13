package com.cinema.service;

import com.cinema.cart.CartItem;
import com.cinema.cart.SessionCart;
import com.cinema.dto.CartItemResponse;
import com.cinema.dto.CartResponse;
import com.cinema.dto.TicketOptionResponse;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.SeatLock;
import com.cinema.entity.TicketType;
import com.cinema.entity.TicketTypeName;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatLockRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final SessionCart sessionCart;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatReservationService seatReservationService;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long screeningId, String sessionId, String username) {
        if (!screeningMatchesSession(screeningId)) {
            log.debug("Cart requested for screening {} but session holds {}", screeningId, sessionCart.getScreeningId());
            return emptyResponse(screeningId);
        }
        refreshLocks(screeningId, sessionId, username);
        if (sessionCart.getScreeningId() == null || sessionCart.getItems().isEmpty()) {
            return emptyResponse(screeningId);
        }
        return buildResponse();
    }

    @Transactional(readOnly = true)
    public List<TicketOptionResponse> getTicketOptions() {
        return ticketTypeRepository.findByActiveTrueOrderByPriceModifier().stream()
            .map(this::mapToTicketOption)
            .collect(Collectors.toList());
    }

    @Transactional
    public CartResponse addSeat(Long screeningId,
                                Long seatId,
                                Long ticketTypeId,
                                String sessionId,
                                String username) {
        Screening screening = getActiveScreening(screeningId);
        Seat seat = getSeat(seatId);
        assertSeatBelongsToScreening(seat, screening);
        TicketType ticketType = getActiveTicketType(ticketTypeId);
        SeatLock lock = findLockForOwner(screeningId, seatId, sessionId, username)
            .orElseThrow(() -> new IllegalStateException("Seat must be locked before it can be added to the cart"));

        alignSessionScreening(screeningId);
        if (sessionCart.getItems().containsKey(seatId)) {
            throw new IllegalStateException("Seat already present in cart");
        }

        CartItem cartItem = CartItem.builder()
            .seatId(seat.getId())
            .rowNumber(seat.getRowNumber())
            .seatNumber(seat.getSeatNumber())
            .ticketType(resolveTicketTypeName(ticketType))
            .ticketTypeId(ticketType.getId())
            .price(calculateSeatPrice(screening, ticketType))
            .lockExpiresAt(lock.getExpiresAt())
            .build();

        sessionCart.getItems().put(seatId, cartItem);
        sessionCart.setScreeningId(screeningId);
        return buildResponse();
    }

    @Transactional
    public CartResponse updateTicketType(Long screeningId,
                                         Long seatId,
                                         Long ticketTypeId,
                                         String sessionId,
                                         String username) {
        ensureCartContainsSeat(screeningId, seatId);
        TicketType ticketType = getActiveTicketType(ticketTypeId);
        Screening screening = getActiveScreening(screeningId);

        CartItem existing = sessionCart.getItems().get(seatId);
        if (existing == null) {
            throw new IllegalArgumentException("Seat not present in cart");
        }

        SeatLock lock = findLockForOwner(screeningId, seatId, sessionId, username)
            .orElseThrow(() -> new IllegalStateException("Seat no longer locked; please lock it again"));

        existing.setTicketType(resolveTicketTypeName(ticketType));
        existing.setTicketTypeId(ticketType.getId());
        existing.setPrice(calculateSeatPrice(screening, ticketType));
        existing.setLockExpiresAt(lock.getExpiresAt());
        return buildResponse();
    }

    @Transactional
    public CartResponse removeSeat(Long screeningId,
                                   Long seatId,
                                   String sessionId,
                                   String username) {
        ensureCartContainsSeat(screeningId, seatId);
        Long fallbackScreeningId = sessionCart.getScreeningId();
        sessionCart.getItems().remove(seatId);
        seatReservationService.releaseSeat(screeningId, seatId, sessionId, username);
        if (sessionCart.getItems().isEmpty()) {
            sessionCart.clear();
            return emptyResponse(fallbackScreeningId);
        }
        return buildResponse();
    }

    private void ensureCartContainsSeat(Long screeningId, Long seatId) {
        if (!screeningMatchesSession(screeningId)) {
            throw new IllegalStateException("Cart is bound to a different screening");
        }
        if (!sessionCart.getItems().containsKey(seatId)) {
            throw new IllegalArgumentException("Seat not present in cart");
        }
    }

    private boolean screeningMatchesSession(Long screeningId) {
        Long current = sessionCart.getScreeningId();
        return current != null && current.equals(screeningId);
    }

    private Screening getActiveScreening(Long screeningId) {
        return screeningRepository.findByIdAndActiveTrue(screeningId)
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", screeningId));
    }

    private Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId)
            .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));
    }

    private TicketType getActiveTicketType(Long ticketTypeId) {
        return ticketTypeRepository.findByIdAndActiveTrue(ticketTypeId)
            .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));
    }

    private void alignSessionScreening(Long screeningId) {
        Long current = sessionCart.getScreeningId();
        if (current == null || !current.equals(screeningId)) {
            sessionCart.clear();
        }
    }

    private void refreshLocks(Long screeningId, String sessionId, String username) {
        Map<Long, SeatLock> locks = findLocksForOwner(screeningId, sessionId, username);
        Iterator<Map.Entry<Long, CartItem>> iterator = sessionCart.getItems().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, CartItem> entry = iterator.next();
            SeatLock lock = locks.get(entry.getKey());
            if (lock == null) {
                iterator.remove();
                continue;
            }
            entry.getValue().setLockExpiresAt(lock.getExpiresAt());
        }
        if (sessionCart.getItems().isEmpty()) {
            sessionCart.clear();
        }
    }

    private void assertSeatBelongsToScreening(Seat seat, Screening screening) {
        if (!seat.getHall().getId().equals(screening.getHall().getId())) {
            throw new IllegalArgumentException("Seat does not belong to selected screening hall");
        }
    }

    private TicketOptionResponse mapToTicketOption(TicketType ticketType) {
        return TicketOptionResponse.builder()
            .ticketTypeId(ticketType.getId())
            .name(resolveTicketTypeName(ticketType))
            .price(ticketType.getPriceModifier())
            .build();
    }

    private CartResponse buildResponse() {
        if (sessionCart.getScreeningId() == null || sessionCart.getItems().isEmpty()) {
            return emptyResponse(sessionCart.getScreeningId());
        }

        List<CartItemResponse> items = sessionCart.getItems().values().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());

        double subtotal = items.stream()
            .mapToDouble(item -> item.getPrice() != null ? item.getPrice() : 0d)
            .sum();

        return CartResponse.builder()
            .screeningId(sessionCart.getScreeningId())
            .items(items)
            .subtotal(subtotal)
            .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
            .seatId(item.getSeatId())
            .rowNumber(item.getRowNumber())
            .seatNumber(item.getSeatNumber())
            .ticketType(item.getTicketType())
            .ticketTypeId(item.getTicketTypeId())
            .price(item.getPrice())
            .lockExpiresAt(item.getLockExpiresAt())
            .build();
    }

    private CartResponse emptyResponse(Long screeningId) {
        return CartResponse.builder()
            .screeningId(screeningId)
            .items(List.of())
            .subtotal(0d)
            .build();
    }

    private TicketTypeName resolveTicketTypeName(TicketType ticketType) {
        try {
            return TicketTypeName.fromString(ticketType.getName());
        } catch (IllegalArgumentException ex) {
            log.warn("Ticket type name {} does not map to enum, defaulting to STANDARD", ticketType.getName());
            return TicketTypeName.STANDARD;
        }
    }

    private double calculateSeatPrice(Screening screening, TicketType ticketType) {
        return ticketType.getPriceModifier();
    }

    private Map<Long, SeatLock> findLocksForOwner(Long screeningId, String sessionId, String username) {
        Map<Long, SeatLock> locks = seatLockRepository.findActiveLocksForSession(screeningId, sessionId, LocalDateTime.now()).stream()
            .collect(Collectors.toMap(lock -> lock.getSeat().getId(), Function.identity()));
        if (hasUsername(username)) {
            List<SeatLock> userLocks = seatLockRepository.findActiveLocksForUsername(screeningId, username, LocalDateTime.now());
            if (userLocks != null) {
                userLocks.forEach(lock -> locks.putIfAbsent(lock.getSeat().getId(), lock));
            }
        }
        return locks;
    }

    private Optional<SeatLock> findLockForOwner(Long screeningId, Long seatId, String sessionId, String username) {
        Optional<SeatLock> lock = seatLockRepository.findActiveLockForSession(screeningId, seatId, sessionId, LocalDateTime.now());
        if (lock.isEmpty() && hasUsername(username)) {
            lock = seatLockRepository.findActiveLockForUsername(screeningId, seatId, username, LocalDateTime.now());
        }
        return lock;
    }

    private boolean hasUsername(String username) {
        return username != null && !username.isBlank();
    }
}
