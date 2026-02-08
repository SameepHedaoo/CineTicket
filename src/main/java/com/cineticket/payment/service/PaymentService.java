package com.cineticket.payment.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.cineticket.booking.dto.BookingResponse;
import com.cineticket.booking.entity.Booking;
import com.cineticket.booking.entity.BookingStatus;
import com.cineticket.booking.entity.PaymentStatus;
import com.cineticket.booking.repository.BookingRepository;
import com.cineticket.booking.service.BookingMapper;
import com.cineticket.show.Service.ShowSeatService;
import com.razorpay.RazorpayClient;
import com.cineticket.payment.config.RazorpayConfig;
import com.cineticket.payment.dto.PaymentRequest;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final ShowSeatService showSeatService;
    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    public PaymentService(BookingRepository bookingRepository,
            ShowSeatService showSeatService,
            RazorpayClient razorpayClient,
            RazorpayConfig razorpayConfig) {
        this.bookingRepository = bookingRepository;
        this.showSeatService = showSeatService;
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
    }

    @Transactional
    public BookingResponse processPayment(PaymentRequest request) {
        if (request == null || request.getBookingId() == null) {
            throw new IllegalArgumentException("bookingId is required");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("payment status is required");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingMapper.toBookingResponse(booking);
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return BookingMapper.toBookingResponse(booking);
        }

        if (booking.getLockExpiryTime() != null
                && booking.getLockExpiryTime().isBefore(LocalDateTime.now())) {
            showSeatService.releaseSeats(booking.getShowSeats());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
            return BookingMapper.toBookingResponse(booking);
        }

        if (request.getStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setStatus(BookingStatus.CONFIRMED);
            showSeatService.markSeatsBooked(booking.getShowSeats());
            return BookingMapper.toBookingResponse(booking);
        }

        if (request.getStatus() == PaymentStatus.FAILED) {
            showSeatService.releaseSeats(booking.getShowSeats());
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setPaymentStatus(PaymentStatus.FAILED);
            return BookingMapper.toBookingResponse(booking);
        }

        throw new IllegalArgumentException("Unsupported payment status");
    }

}
