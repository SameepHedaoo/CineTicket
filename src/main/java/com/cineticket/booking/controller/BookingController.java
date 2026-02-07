package com.cineticket.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.cineticket.booking.dto.BookingResponse;
import com.cineticket.booking.dto.LockSeatsRequest;
import com.cineticket.booking.service.BookingService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(@RequestBody LockSeatsRequest request) {
        return ResponseEntity.ok(bookingService.lockSeats(request, getCurrentUserId()));
    }

    @PostMapping("/initiate")
    public ResponseEntity<BookingResponse> initiate(@RequestBody LockSeatsRequest request) {
        return ResponseEntity.ok(bookingService.lockSeats(request, getCurrentUserId()));
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirm(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @PostMapping("/{bookingId}/payment/initiate")
    public ResponseEntity<BookingResponse> initiatePayment(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.initiatePayment(bookingId));
    }

    @PostMapping("/{bookingId}/payment/confirm")
    public ResponseEntity<BookingResponse> confirmPayment(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.confirmPayment(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> get(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> mine() {
        return ResponseEntity.ok(bookingService.getBookingsForUser(getCurrentUserId()));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return Long.valueOf(auth.getPrincipal().toString());
    }

}
