package com.cineticket.booking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.cineticket.booking.dto.BookingResponse;
import com.cineticket.booking.dto.LockSeatsRequest;
import com.cineticket.booking.entity.Booking;
import com.cineticket.booking.entity.BookingStatus;
import com.cineticket.booking.entity.PaymentStatus;
import com.cineticket.booking.repository.BookingRepository;
import com.cineticket.show.Entity.ShowSeat;
import com.cineticket.show.Service.ShowSeatService;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatService showSeatService;

    private static final Map<String, BigDecimal> SEAT_TYPE_MULTIPLIERS = Map.of(
            "REGULAR", BigDecimal.valueOf(1.0),
            "SILVER", BigDecimal.valueOf(1.15),
            "GOLD", BigDecimal.valueOf(1.30),
            "PREMIUM", BigDecimal.valueOf(1.50),
            "VIP", BigDecimal.valueOf(2.00));

    public BookingService(BookingRepository bookingRepository, ShowSeatService showSeatService) {
        this.bookingRepository = bookingRepository;
        this.showSeatService = showSeatService;
    }

    @Transactional
    public BookingResponse lockSeats(LockSeatsRequest request, Long userId) {
        List<ShowSeat> seats = showSeatService.lockSeats(
                request.getShowId(),
                request.getShowSeatIds());

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShow(seats.get(0).getShow());
        booking.setShowSeats(seats);
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setPaymentStatus(PaymentStatus.NOT_STARTED);
        booking.setLockExpiryTime(LocalDateTime.now().plusMinutes(10));

        Booking saved = bookingRepository.save(booking);
        return mapToBookingResponse(saved);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.IN_PROGRESS
                && booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Invalid state");
        }

        if (booking.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Lock expired");
        }

        showSeatService.markSeatsBooked(booking.getShowSeats());

        if (booking.getPaymentStatus() == null || booking.getPaymentStatus() == PaymentStatus.NOT_STARTED) {
            booking.setPaymentStatus(PaymentStatus.PAID);
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToBookingResponse(booking);
        }

        showSeatService.releaseSeats(booking.getShowSeats());

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        return mapToBookingResponse(booking);
    }

    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Booking is not in progress");
        }

        booking.setPaymentStatus(PaymentStatus.PENDING);
        return mapToBookingResponse(booking);
    }

    @Transactional
    public BookingResponse confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Booking is not in progress");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        showSeatService.markSeatsBooked(booking.getShowSeats());
        return mapToBookingResponse(booking);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        List<Booking> expired = bookingRepository.findByStatusAndLockExpiryTimeBefore(
                BookingStatus.IN_PROGRESS,
                LocalDateTime.now());

        for (Booking booking : expired) {
            showSeatService.releaseSeats(booking.getShowSeats());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (booking.getShow() != null && booking.getShow().getPrice() != null) {
            for (ShowSeat seat : booking.getShowSeats()) {
                String seatType = seat.getSeat().getSeatType();
                BigDecimal multiplier = SEAT_TYPE_MULTIPLIERS.getOrDefault(
                        seatType == null ? "REGULAR" : seatType.toUpperCase(Locale.ROOT),
                        BigDecimal.ONE);
                totalPrice = totalPrice.add(booking.getShow().getPrice().multiply(multiplier));
            }
        }

        List<String> seatNumbers = booking.getShowSeats().stream()
                .map(seat -> String.valueOf(seat.getSeat().getSeatNumber()))
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getShow() == null ? null : booking.getShow().getId(),
                booking.getUserId(),
                seatNumbers,
                totalPrice.doubleValue(),
                booking.getStatus() == null ? null : booking.getStatus().name(),
                booking.getPaymentStatus() == null ? null : booking.getPaymentStatus().name(),
                booking.getLockExpiryTime() == null ? null : booking.getLockExpiryTime().toString());
    }
}
