package com.cineticket.show.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowResponse {
    private Long showId;
    private Long movieId;
    private String movieName;
    private String moviePosterUrl;
    private String theatreName;
    private String screenName;
    private LocalDateTime startTime;
    private BigDecimal price;
    private Integer availableSeats;
    private Integer totalSeats;

    public ShowResponse(Long showId, Long movieId, String movieName, String moviePosterUrl, String theatreName, String screenName,
            LocalDateTime startTime, BigDecimal price,
            Integer availableSeats, Integer totalSeats) {
        this.showId = showId;
        this.movieId = movieId;
        this.movieName = movieName;
        this.moviePosterUrl = moviePosterUrl;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.startTime = startTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;

    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

}
