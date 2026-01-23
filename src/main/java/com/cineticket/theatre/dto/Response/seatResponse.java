package com.cineticket.theatre.dto.Response;

public class seatResponse {
    private Long id;
    private Long seatNumber;
    private String seatType;
    private boolean available;

    public seatResponse(Long id, Long seatNumber, String seatType) {
        this.seatNumber = seatNumber;
        this.seatType = seatType;
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
