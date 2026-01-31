package com.cineticket.booking.dto;

import java.util.List;

public class BookingResponse {
    private Long bookingId;
    private Long showId;
    private List<String> seats;
    private Double totalPrice;
    private String status;

    public BookingResponse(Long bookingId, Long showId, List<String> seats, Double totalPrice, String status) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
