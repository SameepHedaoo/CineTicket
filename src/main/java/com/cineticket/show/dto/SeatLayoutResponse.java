package com.cineticket.show.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SeatLayoutResponse {
    private Long showId;
    private String movieName;
    private String theatreName;
    private String screenName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private List<ShowSeatLayoutResponse> seats;

    public SeatLayoutResponse(Long showId,
            String movieName,
            String theatreName,
            String screenName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal price,
            List<ShowSeatLayoutResponse> seats) {
        this.showId = showId;
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.seats = seats;
    }

    public Long getShowId() {
        return showId;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public String getScreenName() {
        return screenName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public List<ShowSeatLayoutResponse> getSeats() {
        return seats;
    }
}
