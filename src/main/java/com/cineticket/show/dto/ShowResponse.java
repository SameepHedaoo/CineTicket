package com.cineticket.show.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowResponse {
    private Long showId;
    private String movieName;
    private String screenName;
    private LocalDateTime startTime;
    private BigDecimal price;
    private Integer availableSeats;

    public ShowResponse(Long showId, String movieName, String screenName, LocalDateTime startTime, BigDecimal price,
            Integer availableSeats) {
        this.showId = showId;
        this.movieName = movieName;
        this.screenName = screenName;
        this.startTime = startTime;
        this.price = price;
        this.availableSeats = availableSeats;

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

}
