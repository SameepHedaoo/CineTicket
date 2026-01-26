package com.cineticket.show.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cineticket.movie.Entity.MovieEntity;
import com.cineticket.movie.Repository.MovieRepository;
import com.cineticket.show.Entity.Show;
import com.cineticket.show.Repository.ShowRepository;
import com.cineticket.show.dto.ShowRequest;
import com.cineticket.show.dto.ShowResponse;
import com.cineticket.theatre.entity.Screen;
import com.cineticket.theatre.repository.ScreenRepository;
import com.cineticket.theatre.repository.TheatreRepository;

import jakarta.transaction.Transactional;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowService(ShowRepository showRepository, MovieRepository movieRepository,
            ScreenRepository screenRepository, TheatreRepository theatreRepository) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;

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
        return mapToShowResponse(savedShow);
    }

    public void deleteShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new RuntimeException("Show not found");
        }
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

    public Show getShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found"));
    }

    private ShowResponse mapToShowResponse(Show show) {

        return new ShowResponse(
                show.getId(),
                show.getMovie().getTitle(),
                show.getScreen().getScreenName(),
                show.getStartTime(),
                show.getPrice(),
                null);
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

}
