package com.cineticket.movie.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.movie.Service.MovieService;
import com.cineticket.movie.dto.MovieResponse;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/movies")
public class PublicController {

    @Autowired
    MovieService movieService;

    @GetMapping
    // for public
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }
}
