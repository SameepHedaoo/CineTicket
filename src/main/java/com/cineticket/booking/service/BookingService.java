package com.cineticket.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.cineticket.booking.dto.LockSeatsRequest;
import com.cineticket.booking.entity.Booking;
import com.cineticket.booking.entity.BookingStatus;
import com.cineticket.booking.repository.BookingRepository;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.show.Entity.ShowSeat;
import com.cineticket.show.Entity.ShowSeatStatus;
import com.cineticket.show.Repository.ShowSeatRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Booking lockSeats(LockSeatsRequest request, UserEntity user) {

        List<ShowSeat> seats = showSeatRepository.findByIdIn(request.getShowSeatIds());

        for (ShowSeat seat : seats) {
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat not available");
            }
            seat.setStatus(ShowSeatStatus.LOCKED);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(seats.get(0).getShow());
        booking.setShowSeats(seats);
        booking.setStatus(BookingStatus.PENDING);
        booking.setLockExpiryTime(
                LocalDateTime.now().plusMinutes(10));

        return bookingRepository.save(booking);
    }

    // public BookingResponse createBooking(BookingRequest request) {

    // // 1️⃣ Get already booked seats
    // List<List<String>> bookedSeats =
    // bookingRepository.findBookedSeatsByShowId(request.getShowId());

    // Set<String> occupiedSeats = bookedSeats
    // .stream()
    // .flatMap(List::stream)
    // .collect(Collectors.toSet());

    // // 2️⃣ Check seat availability
    // for (String seat : request.getSeats()) {
    // if (occupiedSeats.contains(seat)) {
    // throw new RuntimeException("Seat already booked: " + seat);
    // }
    // }

    // // 3️⃣ Create booking
    // Booking booking = new Booking();
    // booking.setShowId(request.getShowId());
    // booking.setSeats(request.getSeats());
    // booking.setStatus(BookingStatus.CONFIRMED);
    // booking.setBookedAt(LocalDateTime.now());

    // booking.setTotalPrice(request.getSeats().size() * 250.0);

    // Booking saved = bookingRepository.save(booking);

    // // 4️⃣ Response
    // return new BookingResponse(
    // saved.getId(),
    // saved.getShowId(),
    // saved.getSeats(),
    // saved.getTotalPrice(),
    // saved.getStatus().name());
    // }

    @Transactional
    public void confirmBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.PENDING)
            throw new RuntimeException("Invalid state");

        if (booking.getLockExpiryTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Lock expired");

        for (ShowSeat seat : booking.getShowSeats()) {
            seat.setStatus(ShowSeatStatus.BOOKED);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
    }

}
