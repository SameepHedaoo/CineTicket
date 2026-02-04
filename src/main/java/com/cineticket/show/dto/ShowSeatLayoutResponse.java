package com.cineticket.show.dto;

import com.cineticket.show.Entity.ShowSeatStatus;

public class ShowSeatLayoutResponse {
    private Long showSeatId;
    private Long seatId;
    private Long seatNumber;
    private String seatType;
    private ShowSeatStatus status;

    public Long getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Long showSeatId) {
        this.showSeatId = showSeatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Long getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Long seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }
}
