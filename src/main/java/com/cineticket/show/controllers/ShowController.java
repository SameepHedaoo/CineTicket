package com.cineticket.show.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.show.Service.ShowService;
import com.cineticket.show.dto.ShowResponse;
import com.cineticket.show.dto.ShowSeatResponse;

@RestController
@RequestMapping("/shows")
public class ShowController {
    public final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    // GET shows by city
    @GetMapping()
    public List<ShowResponse> getShowsByCity(@RequestParam String city) {
        return showService.getShowsByCity(city);
    }

    @GetMapping("/theatre/{theatreId}")
    public List<ShowResponse> getShowsByTheatre(@PathVariable Long theatreId) {
        return showService.getShowsByTheatre(theatreId);
    }

    @GetMapping("/screen/{screenId}")
    public List<ShowResponse> getShowsByScreen(@PathVariable Long screenId) {
        return showService.getShowsByScreen(screenId);
    }

    @GetMapping("/by-movie-theatre")
    public List<ShowResponse> getShowsByMovieAndTheatre(
            @RequestParam Long movieId,
            @RequestParam Long theatreId) {
        return showService.getShowsByMovieAndTheatre(movieId, theatreId);
    }

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponse> getSeats(@PathVariable Long showId) {
        return showService.getShowSeats(showId);
    }

}
