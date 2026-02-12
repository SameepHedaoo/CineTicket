package com.cineticket.show.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cineticket.movie.Entity.MovieEntity;
import com.cineticket.movie.Repository.MovieRepository;
import com.cineticket.show.Entity.Show;
import com.cineticket.show.Entity.ShowSeat;
import com.cineticket.show.Entity.ShowSeatStatus;
import com.cineticket.show.Repository.ShowRepository;
import com.cineticket.show.Repository.ShowSeatRepository;
import com.cineticket.show.dto.ShowRequest;
import com.cineticket.show.dto.ShowResponse;
import com.cineticket.show.dto.ShowSeatResponse;
import com.cineticket.show.dto.ShowSeatLayoutResponse;
import com.cineticket.show.dto.SeatLayoutResponse;
import com.cineticket.theatre.entity.Screen;
import com.cineticket.theatre.entity.Seat;
import com.cineticket.theatre.repository.ScreenRepository;
import com.cineticket.theatre.repository.SeatRepository;
import com.cineticket.theatre.repository.TheatreRepository;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.transaction.Transactional;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final UserRepository userRepository;
    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private SeatRepository seatRepository;

    public ShowService(ShowRepository showRepository, MovieRepository movieRepository,
            ScreenRepository screenRepository, TheatreRepository theatreRepository,
            UserRepository userRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.userRepository = userRepository;

    }

    // ADMIN METHODS
    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        // time
        if (request.getStartTime().isAfter(request.getEndTime())
                || request.getStartTime().isEqual(request.getEndTime())) {
            throw new IllegalArgumentException("Invalid show time");
        }
        // fetch movie
        MovieEntity movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        // fetch screen
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));
        enforceManagerTheatreAccess(screen.getTheatre() != null ? screen.getTheatre().getId() : null);
        // Overlapping time
        List<Show> overlappingShows = showRepository.findOverlappingShows(
                screen.getId(),
                request.getStartTime(),
                request.getEndTime());

        if (!overlappingShows.isEmpty()) {
            throw new IllegalStateException(
                    "Another show already exists on this screen during this time");
        }
        // create show
        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setPrice(request.getPrice());

        // save
        Show savedShow = showRepository.save(show);
        List<Seat> seats = seatRepository
                .findByScreenId(savedShow.getScreen().getId());

        for (Seat seat : seats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(savedShow);
            showSeat.setSeat(seat);
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeatRepository.save(showSeat);
        }
        return mapToShowResponse(savedShow);
    }

    public void deleteShow(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));
        Long theatreId = show.getScreen() != null && show.getScreen().getTheatre() != null
                ? show.getScreen().getTheatre().getId()
                : null;
        enforceManagerTheatreAccess(theatreId);
        showRepository.deleteById(showId);
    }

    // {PUBLIC}
    public List<ShowResponse> getShowsByCity(String city) {

        List<Show> shows = showRepository.findByScreenTheatreCity(city);
        List<ShowResponse> response = new ArrayList<>();

        for (Show show : shows) {
            response.add(mapToShowResponse(show));
        }

        return response;
    }

    public List<ShowResponse> getShowsByTheatre(Long theatreId) {

        List<Show> shows = showRepository.findByScreen_Theatre_Id(theatreId);

        List<ShowResponse> response = new ArrayList<>();

        for (Show show : shows) {
            response.add(mapToShowResponse(show));
        }

        return response;
    }

    public List<ShowResponse> getShowsByScreen(Long screenId) {

        List<Show> shows = showRepository.findByScreenId(screenId);

        List<ShowResponse> response = new ArrayList<>();

        for (Show show : shows) {
            response.add(mapToShowResponse(show));
        }

        return response;
    }

    public List<ShowResponse> getShowsByMovie(Long movieId) {
        List<Show> shows = showRepository.findByMovieId(movieId);
        List<ShowResponse> response = new ArrayList<>();
        for (Show show : shows) {
            response.add(mapToShowResponse(show));
        }
        return response;
    }

    public Show getShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));
    }

    private ShowResponse mapToShowResponse(Show show) {
        Long showId = show.getId();
        int totalSeats = showId == null ? 0 : Math.toIntExact(showSeatRepository.countByShowId(showId));
        int availableSeats = showId == null
                ? 0
                : Math.toIntExact(showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.AVAILABLE));

        String theatreName = show.getScreen() != null
                && show.getScreen().getTheatre() != null
                        ? show.getScreen().getTheatre().getName()
                        : null;

        return new ShowResponse(
                showId,
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getMovie().getPosterUrl(),
                theatreName,
                show.getScreen().getScreenName(),
                show.getStartTime(),
                show.getPrice(),
                availableSeats,
                totalSeats);
    }

    public List<ShowResponse> getShowsByMovieAndTheatre(Long movieId, Long theatreId) {
        List<Show> shows = showRepository.findByMovieIdAndScreen_Theatre_Id(movieId, theatreId);

        if (shows.isEmpty()) {
            return shows.stream()
                    .map(this::mapToShowResponse)
                    .toList();
        }

        List<ShowResponse> response = new ArrayList<>();
        for (Show show : shows) {
            response.add(mapToShowResponse(show));
        }

        return response;

    }

    public List<ShowSeatResponse> getShowSeats(Long showId) {

        List<ShowSeat> seats = showSeatRepository.findByShowId(showId);

        return seats.stream().map(seat -> {
            ShowSeatResponse dto = new ShowSeatResponse();
            dto.setShowSeatId(seat.getId());
            dto.setSeatNumber(seat.getSeat().getSeatNumber());
            dto.setStatus(seat.getStatus());
            return dto;
        }).toList();
    }

    public SeatLayoutResponse getSeatLayout(Long showId) {
        List<ShowSeat> seats = showSeatRepository.findByShowId(showId);
        List<ShowSeatLayoutResponse> layout = seats.stream().map(seat -> {
            ShowSeatLayoutResponse dto = new ShowSeatLayoutResponse();
            dto.setShowSeatId(seat.getId());
            dto.setSeatId(seat.getSeat().getId());
            dto.setSeatNumber(seat.getSeat().getSeatNumber());
            dto.setSeatType(seat.getSeat().getSeatType());
            dto.setStatus(seat.getStatus());
            return dto;
        }).toList();
        Show show = seats.isEmpty() ? getShowById(showId) : seats.get(0).getShow();
        String screenName = show.getScreen() != null ? show.getScreen().getScreenName() : null;
        String theatreName = show.getScreen() != null && show.getScreen().getTheatre() != null
                ? show.getScreen().getTheatre().getName()
                : null;
        String movieName = show.getMovie() != null ? show.getMovie().getTitle() : null;
        return new SeatLayoutResponse(
                showId,
                movieName,
                theatreName,
                screenName,
                show.getStartTime(),
                show.getEndTime(),
                show.getPrice(),
                layout);
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
