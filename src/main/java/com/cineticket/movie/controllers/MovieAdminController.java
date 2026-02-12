package com.cineticket.movie.controllers;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cineticket.movie.Service.MovieService;
import com.cineticket.movie.dto.MovieRequest;
import com.cineticket.movie.dto.MovieResponse;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/admin/movies")
public class MovieAdminController {

    private final MovieService movieService;

    public MovieAdminController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add")
    public MovieResponse addMovie(@RequestBody MovieRequest request) {
        return movieService.addMovie(request);
    }

    @PostMapping(value = "/upload-poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPoster(@RequestPart("file") MultipartFile file) {
        String posterUrl = movieService.uploadPoster(file);
        return ResponseEntity.ok(Map.of("posterUrl", posterUrl));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }
}
