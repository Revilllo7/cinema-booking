package com.cinema.service;

import com.cinema.dto.BookingDTO;
import com.cinema.entity.Booking;
import com.cinema.entity.BookingSeat;
import com.cinema.entity.Screening;
import com.cinema.entity.Seat;
import com.cinema.entity.TicketType;
import com.cinema.entity.User;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.ScreeningRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.TicketTypeRepository;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        log.debug("Fetching all bookings with pagination: {}", pageable);
        return bookingRepository.findAll(pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        log.debug("Fetching booking by id: {}", id);
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return convertToDto(booking);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingByBookingNumber(String bookingNumber) {
        log.debug("Fetching booking by booking number: {}", bookingNumber);
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingNumber", bookingNumber));
        return convertToDto(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByUser(Long userId, Pageable pageable) {
        log.debug("Fetching bookings for user id: {}", userId);
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return bookingRepository.findByUserId(userId, pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByScreening(Long screeningId) {
        log.debug("Fetching bookings for screening id: {}", screeningId);
        return bookingRepository.findByScreeningId(screeningId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByStatus(Booking.BookingStatus status, Pageable pageable) {
        log.debug("Fetching bookings with status: {}", status);
        return bookingRepository.findByStatus(status, pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching bookings between {} and {}", startDate, endDate);
        return bookingRepository.findByCreatedAtBetween(startDate, endDate)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.info("Creating new booking for user id: {}, screening id: {}", 
            bookingDTO.getUserId(), bookingDTO.getScreeningId());

        // Validate user exists
        User user = userRepository.findById(bookingDTO.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", bookingDTO.getUserId()));

        // Validate screening exists
        Screening screening = screeningRepository.findById(bookingDTO.getScreeningId())
            .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", bookingDTO.getScreeningId()));

        // Validate seats
        if (bookingDTO.getSeats() == null || bookingDTO.getSeats().isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }

        // Create booking
        Booking booking = Booking.builder()
            .user(user)
            .screening(screening)
            .customerEmail(bookingDTO.getCustomerEmail())
            .customerPhone(bookingDTO.getCustomerPhone())
            .status(Booking.BookingStatus.PENDING)
            .bookingSeats(new HashSet<>())
            .build();

        // Add booking seats
        double totalPrice = 0;
        for (BookingDTO.BookingSeatRequest seatRequest : bookingDTO.getSeats()) {
            Seat seat = seatRepository.findById(seatRequest.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatRequest.getSeatId()));

            TicketType ticketType = ticketTypeRepository.findById(seatRequest.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", seatRequest.getTicketTypeId()));

            // Check if seat is already booked
            boolean seatAlreadyBooked = booking.getBookingSeats().stream()
                .anyMatch(bs -> bs.getSeat().getId().equals(seatRequest.getSeatId()));
            if (seatAlreadyBooked) {
                throw new IllegalArgumentException("Seat already selected: " + seatRequest.getSeatId());
            }

            BookingSeat bookingSeat = BookingSeat.builder()
                .booking(booking)
                .seat(seat)
                .ticketType(ticketType)
                .price(ticketType.getPriceModifier())
                .build();

            booking.getBookingSeats().add(bookingSeat);
            totalPrice += ticketType.getPriceModifier();
        }

        booking.setTotalPrice(totalPrice);
        booking.setPaymentMethod(bookingDTO.getPaymentMethod());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with id: {} and booking number: {}", 
            savedBooking.getId(), savedBooking.getBookingNumber());

        return convertToDto(savedBooking);
    }

    @Transactional
    public BookingDTO confirmBooking(Long id, String paymentMethod, String paymentReference) {
        log.info("Confirming booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getStatus().equals(Booking.BookingStatus.PENDING)) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentReference(paymentReference);

        Booking confirmedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed successfully: {}", confirmedBooking.getId());

        return convertToDto(confirmedBooking);
    }

    @Transactional
    public BookingDTO cancelBooking(Long id, String reason) {
        log.info("Cancelling booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus().equals(Booking.BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully: {}", cancelledBooking.getId());

        return convertToDto(cancelledBooking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting booking with id: {}", id);
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking", "id", id);
        }
        bookingRepository.deleteById(id);
        log.info("Booking deleted successfully: {}", id);
    }

    // Mapping methods
    private BookingDTO convertToDto(Booking booking) {
        return BookingDTO.builder()
            .id(booking.getId())
            .bookingNumber(booking.getBookingNumber())
            .userId(booking.getUser().getId())
            .screeningId(booking.getScreening().getId())
            .seats(booking.getBookingSeats().stream()
                .map(bs -> new BookingDTO.BookingSeatRequest(
                    bs.getSeat().getId(),
                    bs.getTicketType().getId()
                ))
                .collect(Collectors.toList()))
            .totalPrice(booking.getTotalPrice())
            .status(booking.getStatus().toString())
            .paymentMethod(booking.getPaymentMethod())
            .customerEmail(booking.getCustomerEmail())
            .customerPhone(booking.getCustomerPhone())
            .createdAt(booking.getCreatedAt())
            .build();
    }

    private Booking convertToEntity(BookingDTO dto) {
        // TODO: will resolve with REST
        return Booking.builder()
            .customerEmail(dto.getCustomerEmail())
            .customerPhone(dto.getCustomerPhone())
            .paymentMethod(dto.getPaymentMethod())
            .status(Booking.BookingStatus.PENDING)
            .build();
    }

    private void updateEntityFromDto(Booking booking, BookingDTO dto) {
        // TODO: will resolve with REST
        booking.setCustomerEmail(dto.getCustomerEmail());
        booking.setCustomerPhone(dto.getCustomerPhone());
        // Don't update status directly - use confirmBooking, cancelBooking methods
        // Don't update seats after creation - delete and recreate
    }
}
