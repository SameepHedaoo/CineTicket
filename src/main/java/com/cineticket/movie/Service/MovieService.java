package com.cineticket.movie.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.cineticket.movie.Entity.MovieEntity;
import com.cineticket.movie.Repository.MovieRepository;
import com.cineticket.movie.dto.MovieRequest;
import com.cineticket.movie.dto.MovieResponse;
import com.cineticket.show.Entity.Show;
import com.cineticket.show.Repository.ShowRepository;
import com.cineticket.show.Repository.ShowSeatRepository;
import com.cineticket.booking.repository.BookingRepository;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    public MovieService(MovieRepository movieRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            BookingRepository bookingRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
    }

    // ADMIN
    public MovieResponse addMovie(MovieRequest movieRequest) {
        MovieEntity movie = new MovieEntity();
        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setDuration(movieRequest.getDurationMinutes());
        movie.setGenre(movieRequest.getGenre());

        MovieEntity saved = movieRepository.save(movie);
        return new MovieResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getLanguage(),
                saved.getDuration(),
                saved.getGenre());
    }

    @Transactional
    public void deleteMovie(Long movieId) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        List<Show> shows = showRepository.findByMovieId(movieId);
        for (Show show : shows) {
            bookingRepository.deleteByShow_Id(show.getId());
            showSeatRepository.deleteByShowId(show.getId());
            showRepository.deleteById(show.getId());
        }
        if (!movie.isActive()) {
            return;
        }
        movie.setActive(false);
        movieRepository.save(movie);
    }

    // PUBLIC
    public List<MovieResponse> getAllMovies() {
        List<MovieEntity> movies = movieRepository.findByActiveTrue();
        List<MovieResponse> responses = new ArrayList<>();
        for (MovieEntity m : movies) {
            MovieResponse response = new MovieResponse(m.getId(), m.getTitle(), m.getDescription(), m.getLanguage(),
                    m.getDuration(), m.getGenre());
            responses.add(response);
        }
        return responses;
    }
}
