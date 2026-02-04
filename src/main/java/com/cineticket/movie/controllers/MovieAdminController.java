package com.cineticket.movie.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.movie.Service.MovieService;
import com.cineticket.movie.dto.MovieRequest;
import com.cineticket.movie.dto.MovieResponse;

@RestController
@RequestMapping("/admin/movies")
public class MovieAdminController {

    private final MovieService movieService;

    // ✅ Correct constructor
    public MovieAdminController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add")
    public MovieResponse addMovie(@RequestBody MovieRequest request) {
        return movieService.addMovie(request);
    }
}
