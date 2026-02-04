package com.cineticket.show.dto;

import java.util.List;

public class SeatLayoutResponse {
    private Long showId;
    private String screenName;
    private List<ShowSeatLayoutResponse> seats;

    public SeatLayoutResponse(Long showId, String screenName, List<ShowSeatLayoutResponse> seats) {
        this.showId = showId;
        this.screenName = screenName;
        this.seats = seats;
    }

    public Long getShowId() {
        return showId;
    }

    public String getScreenName() {
        return screenName;
    }

    public List<ShowSeatLayoutResponse> getSeats() {
        return seats;
    }
}
