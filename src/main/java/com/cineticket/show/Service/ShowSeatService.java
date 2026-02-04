package com.cineticket.show.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cineticket.show.Entity.ShowSeat;
import com.cineticket.show.Entity.ShowSeatStatus;
import com.cineticket.show.Repository.ShowSeatRepository;

import jakarta.transaction.Transactional;

@Service
public class ShowSeatService {
    private final ShowSeatRepository showSeatRepository;

    public ShowSeatService(ShowSeatRepository showSeatRepository) {
        this.showSeatRepository = showSeatRepository;
    }

    @Transactional
    public List<ShowSeat> lockSeats(Long showId, List<Long> showSeatIds) {
        if (showId == null) {
            throw new IllegalArgumentException("showId is required");
        }
        if (showSeatIds == null || showSeatIds.isEmpty()) {
            throw new IllegalArgumentException("showSeatIds is required");
        }

        List<ShowSeat> seats = showSeatRepository.findByIdIn(showSeatIds);
        if (seats.size() != showSeatIds.size()) {
            throw new RuntimeException("One or more seats not found");
        }

        for (ShowSeat seat : seats) {
            if (!seat.getShow().getId().equals(showId)) {
                throw new RuntimeException("Seat does not belong to the requested show");
            }
        }

        for (ShowSeat seat : seats) {
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat not available");
            }
            seat.setStatus(ShowSeatStatus.LOCKED);
        }

        return seats;
    }

    public void markSeatsBooked(List<ShowSeat> seats) {
        for (ShowSeat seat : seats) {
            seat.setStatus(ShowSeatStatus.BOOKED);
        }
    }

    public void releaseSeats(List<ShowSeat> seats) {
        for (ShowSeat seat : seats) {
            seat.setStatus(ShowSeatStatus.AVAILABLE);
        }
    }
}
