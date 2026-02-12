package com.cineticket.movie.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final String uploadDir;
    private final GitHubImageStorageService gitHubImageStorageService;

    public MovieService(MovieRepository movieRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            BookingRepository bookingRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir,
            GitHubImageStorageService gitHubImageStorageService) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
        this.uploadDir = uploadDir;
        this.gitHubImageStorageService = gitHubImageStorageService;
    }

    // ADMIN
    public MovieResponse addMovie(MovieRequest movieRequest) {
        MovieEntity movie = new MovieEntity();
        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setDuration(movieRequest.getDurationMinutes());
        movie.setGenre(movieRequest.getGenre());
        movie.setPosterUrl(movieRequest.getPosterUrl());

        MovieEntity saved = movieRepository.save(movie);
        return new MovieResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getLanguage(),
                saved.getDuration(),
                saved.getGenre(),
                saved.getPosterUrl());
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
                    m.getDuration(), m.getGenre(), m.getPosterUrl());
            responses.add(response);
        }
        return responses;
    }

    public String uploadPoster(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Poster file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        long maxBytes = 5L * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("Poster must be 5 MB or less");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read poster file", ex);
        }

        if (gitHubImageStorageService.isEnabled()) {
            return gitHubImageStorageService.uploadPoster(bytes, fileName);
        }

        try {
            Path posterDir = Paths.get(uploadDir, "posters").toAbsolutePath().normalize();
            Files.createDirectories(posterDir);
            Path target = posterDir.resolve(fileName).normalize();
            Files.write(target, bytes);
            return "/uploads/posters/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store poster", ex);
        }
    }

    private String getFileExtension(String originalFileName) {
        if (originalFileName == null) {
            return ".jpg";
        }
        int idx = originalFileName.lastIndexOf('.');
        if (idx < 0 || idx == originalFileName.length() - 1) {
            return ".jpg";
        }
        String ext = originalFileName.substring(idx).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return ".jpg";
        }
        return ext;
    }
}
