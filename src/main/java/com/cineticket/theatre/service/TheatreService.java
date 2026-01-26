package com.cineticket.theatre.service;

import com.cineticket.theatre.dto.Request.ScreenRequest;
import com.cineticket.theatre.dto.Request.TheatreRequest;
import com.cineticket.theatre.dto.Response.ScreenResponse;
import com.cineticket.theatre.dto.Response.SeatResponse;
import com.cineticket.theatre.dto.Response.TheatreResponse;
import com.cineticket.theatre.entity.Screen;
import com.cineticket.theatre.entity.Seat;
import com.cineticket.theatre.entity.Theatre;
import com.cineticket.theatre.repository.ScreenRepository;
import com.cineticket.theatre.repository.SeatRepository;
import com.cineticket.theatre.repository.TheatreRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    public TheatreService(TheatreRepository theatreRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository) {
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
    }

    public List<TheatreResponse> getTheatresByCity(String city) {
        List<Theatre> theatres = theatreRepository.findByCitySafe(city);
        List<TheatreResponse> response = new ArrayList<>();

        for (Theatre theatre : theatres) {
            response.add(mapToTheatreResponse(theatre));
        }

        return response;
    }

    private TheatreResponse mapToTheatreResponse(Theatre theatre) {
        List<ScreenResponse> screens = new ArrayList<>();

        for (Screen screen : theatre.getScreens()) {
            List<SeatResponse> seats = new ArrayList<>();
            for (Seat seat : screen.getSeats()) {
                seats.add(new SeatResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getSeatType(),
                        seat.isActive()));
            }
            screens.add(new ScreenResponse(
                    screen.getId(),
                    screen.getScreenName(),
                    seats));
        }

        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getCity(),
                theatre.getAddress(),
                screens);
    }

    // ADMIN
    public void addTheatre(TheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setCity(request.getCity());
        theatre.setAddress(request.getAddress());
        theatreRepository.save(theatre);
    }

    @Transactional
    public ScreenResponse createScreen(ScreenRequest request) {

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        // Create Screen
        Screen screen = new Screen();
        screen.setScreenName(request.getName());
        screen.setTheatre(theatre);

        Screen savedScreen = screenRepository.save(screen);

        // Create Seats
        List<Seat> seats = new ArrayList<>();

        for (long i = 1; i <= request.getTotalSeats(); i++) {
            Seat seat = new Seat();
            seat.setSeatNumber(i); // ✅ long
            seat.setSeatType("REGULAR");
            seat.setActive(true);
            seat.setScreen(savedScreen);

            seats.add(seat);
        }

        seatRepository.saveAll(seats);
        savedScreen.setSeats(seats);

        // Map Seats → Response
        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getSeatType(),
                        seat.isActive()))
                .toList();

        return new ScreenResponse(
                savedScreen.getId(),
                savedScreen.getScreenName(),
                seatResponses);
    }

}
