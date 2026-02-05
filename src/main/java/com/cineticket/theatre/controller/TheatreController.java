package com.cineticket.theatre.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.cineticket.theatre.dto.Request.ScreenRequest;
import com.cineticket.theatre.dto.Request.TheatreRequest;
import com.cineticket.theatre.dto.Response.ScreenResponse;
import com.cineticket.theatre.dto.Response.TheatreResponse;
import com.cineticket.theatre.entity.Theatre;
import com.cineticket.theatre.repository.TheatreRepository;
import com.cineticket.theatre.service.TheatreService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/theatres")
public class TheatreController {
    private final TheatreService theatreService;
    private final TheatreRepository theatreRepository;

    public TheatreController(TheatreService theatreService, TheatreRepository theatreRepository) {
        this.theatreService = theatreService;
        this.theatreRepository = theatreRepository;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    // get theatres by city
    @GetMapping()
    public List<TheatreResponse> getTheatrebycity(@RequestParam String city) {
        return theatreService.getTheatresByCity(city);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/{theatreId}")
    public TheatreResponse getTheatreById(@PathVariable Long theatreId) {
        return theatreService.getTheatreById(theatreId);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/names")
    public List<String> getTheatreNamesByCity(@RequestParam String city) {
        List<Theatre> theatres = theatreRepository.findByCitySafe(city);
        return theatres.stream().map(Theatre::getName).toList();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    // POST - add a theatre
    @PostMapping("/add")
    public TheatreResponse addTheatre(@RequestBody TheatreRequest request) {
        return theatreService.addTheatre(request);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    // Post - add a screen
    @PostMapping("/screens")
    public ScreenResponse addScreen(@RequestBody ScreenRequest request) {
        return theatreService.createScreen(request);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/{theatreId}")
    public ResponseEntity<Void> deleteTheatre(@PathVariable Long theatreId) {
        theatreService.deleteTheatre(theatreId);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/screens/{screenId}")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long screenId) {
        theatreService.deleteScreen(screenId);
        return ResponseEntity.noContent().build();
    }

}
