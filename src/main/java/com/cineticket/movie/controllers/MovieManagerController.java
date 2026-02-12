package com.cineticket.movie.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;
import com.cineticket.movie.Service.MovieService;
import com.cineticket.movie.dto.MovieResponse;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/movies")
public class MovieManagerController {

    private final MovieService movieService;
    private final UserRepository userRepository;

    public MovieManagerController(MovieService movieService, UserRepository userRepository) {
        this.movieService = movieService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/upload-poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPoster(@RequestPart("file") MultipartFile file) {
        enforceManagerOrAdmin();
        String posterUrl = movieService.uploadPoster(file);
        return ResponseEntity.ok(Map.of("posterUrl", posterUrl));
    }

    @PostMapping(value = "/{movieId}/poster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MovieResponse updateMoviePoster(@PathVariable Long movieId, @RequestPart("file") MultipartFile file) {
        enforceManagerOrAdmin();
        String posterUrl = movieService.uploadPoster(file);
        return movieService.updateMoviePoster(movieId, posterUrl);
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        enforceManagerOrAdmin();
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    private void enforceManagerOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.THEATRE_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin/manager can modify movies");
        }
    }
}
