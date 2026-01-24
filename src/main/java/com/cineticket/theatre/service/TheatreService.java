package com.cineticket.theatre.service;

import com.cineticket.theatre.dto.Request.TheatreRequest;
import com.cineticket.theatre.dto.Response.ScreenResponse;
import com.cineticket.theatre.dto.Response.SeatResponse;
import com.cineticket.theatre.dto.Response.TheatreResponse;
import com.cineticket.theatre.entity.Screen;
import com.cineticket.theatre.entity.Seat;
import com.cineticket.theatre.entity.Theatre;
import com.cineticket.theatre.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
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
}
