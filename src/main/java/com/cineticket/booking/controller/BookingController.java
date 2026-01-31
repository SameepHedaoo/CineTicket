package com.cineticket.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.booking.dto.LockSeatsRequest;
import com.cineticket.booking.entity.Booking;
import com.cineticket.booking.service.BookingService;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> book(@RequestBody LockSeatsRequest request) {
        return ResponseEntity.ok(bookingService.lockSeats(request, null));
    }

    @PostMapping("/{bookingId}/confirm")
    public void confirm(@PathVariable Long bookingId) {
        bookingService.confirmBooking(bookingId);
    }

}
