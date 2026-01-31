package com.cineticket.show.dto;

import com.cineticket.show.Entity.ShowSeatStatus;

public class ShowSeatResponse {
    private Long showSeatId;
    private Long seatNumber;
    private ShowSeatStatus status;

    public Long getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Long showSeatId) {
        this.showSeatId = showSeatId;
    }

    public Long getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Long seatNumber) {
        this.seatNumber = seatNumber;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

}
