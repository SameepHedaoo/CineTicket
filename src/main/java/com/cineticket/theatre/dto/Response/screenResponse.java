package com.cineticket.theatre.dto.Response;

import java.util.List;

public class ScreenResponse {
    private Long id;
    private String name;
    private List<SeatResponse> seats;

    public ScreenResponse(Long id, String name, List<SeatResponse> seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatResponse> seats) {
        this.seats = seats;
    }

}
