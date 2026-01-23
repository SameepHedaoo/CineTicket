package com.cineticket.theatre.dto.Response;

import java.util.List;

public class screenResponse {
    private Long id;
    private String name;
    private long totalSeats;
    private List<seatResponse> seats;

    public screenResponse(Long id, String name, Long totalSeats, List<seatResponse> seats) {
        this.name = name;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public List<seatResponse> getSeats() {
        return seats;
    }
}
