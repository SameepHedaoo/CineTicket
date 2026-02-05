package com.cineticket.booking.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        return BookingMapper.toBookingResponse(saved);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return BookingMapper.toBookingResponse(booking);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is cancelled");
        }

        if (booking.getStatus() != BookingStatus.IN_PROGRESS
                && booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Invalid state");
        }

        if (booking.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            showSeatService.releaseSeats(booking.getShowSeats());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
            throw new RuntimeException("Lock expired");
        }

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Payment not completed");
        }

        showSeatService.markSeatsBooked(booking.getShowSeats());
        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingMapper.toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingMapper.toBookingResponse(booking);
        }

        showSeatService.releaseSeats(booking.getShowSeats());

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.FAILED);
        return BookingMapper.toBookingResponse(booking);
    }

    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();
        return BookingMapper.toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Booking is not in progress");
        }

        booking.setPaymentStatus(PaymentStatus.PENDING);
        return BookingMapper.toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Booking is not in progress");
        }

        if (booking.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            showSeatService.releaseSeats(booking.getShowSeats());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
            throw new RuntimeException("Lock expired");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        showSeatService.markSeatsBooked(booking.getShowSeats());
        return BookingMapper.toBookingResponse(booking);
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
}
