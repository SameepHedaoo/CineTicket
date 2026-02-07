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
import com.cineticket.show.Entity.Show;
import com.cineticket.show.Repository.ShowRepository;
import com.cineticket.show.Repository.ShowSeatRepository;
import com.cineticket.booking.repository.BookingRepository;
import com.cineticket.city.service.CityService;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CityService cityService;

    public TheatreService(TheatreRepository theatreRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CityService cityService) {
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.cityService = cityService;
    }

    public List<TheatreResponse> getTheatresByCity(String city) {
        List<Theatre> theatres = theatreRepository.findByCitySafe(city);
        List<TheatreResponse> response = new ArrayList<>();

        for (Theatre theatre : theatres) {
            response.add(mapToTheatreResponse(theatre));
        }

        return response;
    }

    public TheatreResponse getTheatreById(Long theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found"));
        return mapToTheatreResponse(theatre);
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
    public TheatreResponse addTheatre(TheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setCity(request.getCity());
        theatre.setAddress(request.getAddress());
        cityService.ensureCityExists(request.getCity());
        Theatre saved = theatreRepository.save(theatre);
        return new TheatreResponse(
                saved.getId(),
                saved.getName(),
                saved.getCity(),
                saved.getAddress(),
                new ArrayList<>());
    }

    @Transactional
    public ScreenResponse createScreen(ScreenRequest request) {

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found"));
        enforceManagerTheatreAccess(theatre.getId());

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

    @Transactional
    public void deleteScreen(Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new RuntimeException("Screen not found"));
        Long theatreId = screen.getTheatre() != null ? screen.getTheatre().getId() : null;
        enforceManagerTheatreAccess(theatreId);
        List<Show> shows = showRepository.findByScreenId(screenId);
        for (Show show : shows) {
            bookingRepository.deleteByShow_Id(show.getId());
            showSeatRepository.deleteByShowId(show.getId());
            showRepository.deleteById(show.getId());
        }
        seatRepository.deleteByScreenId(screenId);
        screenRepository.delete(screen);
    }

    @Transactional
    public void deleteTheatre(Long theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found"));
        List<Show> shows = showRepository.findByScreen_Theatre_Id(theatreId);
        for (Show show : shows) {
            bookingRepository.deleteByShow_Id(show.getId());
            showSeatRepository.deleteByShowId(show.getId());
            showRepository.deleteById(show.getId());
        }
        theatreRepository.delete(theatre);
    }

    private void enforceManagerTheatreAccess(Long theatreId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return;
        }
        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_THEATRE_MANAGER".equals(a.getAuthority()));
        if (!isManager) {
            return;
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof Long)) {
            throw new RuntimeException("Unauthorized");
        }
        UserEntity user = userRepository.findById((Long) principal)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.THEATRE_MANAGER) {
            return;
        }
        if (theatreId == null || user.getTheatreId() == null || !theatreId.equals(user.getTheatreId())) {
            throw new RuntimeException("Not allowed for this theatre");
        }
    }

}
