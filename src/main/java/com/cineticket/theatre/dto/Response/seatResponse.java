package com.cineticket.theatre.dto.Response;

public class SeatResponse {
    private Long id;
    private Long seatNumber;
    private String seatType;
    private boolean available;

    public SeatResponse(Long id, Long seatNumber, String seatType, boolean available) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.available = available;
    }

    public Long getSeatNumber() {
        return seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public Long getId() {
        return id;
    }

    public boolean isAvailable() {
        return available;
    }
}
