package com.cineticket.movie.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cineticket.movie.Entity.MovieEntity;
import com.cineticket.movie.Repository.MovieRepository;
import com.cineticket.movie.dto.MovieRequest;
import com.cineticket.movie.dto.MovieResponse;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
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
