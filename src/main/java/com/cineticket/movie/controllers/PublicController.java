package com.cineticket.movie.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.movie.Entity.MovieEntity;
import com.cineticket.movie.Repository.MovieRepository;
import com.cineticket.movie.Service.MovieService;
import com.cineticket.movie.dto.MovieResponse;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/movies")
public class PublicController {

    @Autowired
    MovieService movieService;
    @Autowired
    MovieRepository movieRepository;

    @GetMapping("/all")
    // for public
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping
    public List<MovieEntity> getMoviesTabMovies() {
        return movieRepository.findDistinctMoviesForMoviesTab();
    }
}
