package com.cineticket.theatre.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.theatre.dto.Request.ScreenRequest;
import com.cineticket.theatre.dto.Request.TheatreRequest;
import com.cineticket.theatre.dto.Response.ScreenResponse;
import com.cineticket.theatre.dto.Response.TheatreResponse;
import com.cineticket.theatre.entity.Theatre;
import com.cineticket.theatre.repository.TheatreRepository;
import com.cineticket.theatre.service.TheatreService;

@RestController
@RequestMapping("/theatres")
public class TheatreController {
    private final TheatreService theatreService;
    private final TheatreRepository theatreRepository;

    public TheatreController(TheatreService theatreService, TheatreRepository theatreRepository) {
        this.theatreService = theatreService;
        this.theatreRepository = theatreRepository;
    }

    // get theatres by city
    @GetMapping()
    public List<TheatreResponse> getTheatrebycity(@RequestParam String city) {
        return theatreService.getTheatresByCity(city);
    }

    @GetMapping("/names")
    public List<String> getTheatreNamesByCity(@RequestParam String city) {
        List<Theatre> theatres = theatreRepository.findByCitySafe(city);
        return theatres.stream().map(Theatre::getName).toList();
    }

    // POST - add a theatre
    @PostMapping("/add")
    public TheatreResponse addTheatre(@RequestBody TheatreRequest request) {
        return theatreService.addTheatre(request);
    }

    // Post - add a screen
    @PostMapping("/screens")
    public ScreenResponse addScreen(@RequestBody ScreenRequest request) {
        return theatreService.createScreen(request);
    }

}
