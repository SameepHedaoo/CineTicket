package com.cineticket.booking.dto;

import java.util.List;

public class BookingResponse {
    private Long bookingId;
    private Long showId;
    private Long userId;
    private List<String> seats;
    private Double totalPrice;
    private String status;
    private String paymentStatus;
    private String lockExpiryTime;

    public BookingResponse(Long bookingId, Long showId, Long userId, List<String> seats, Double totalPrice,
            String status, String paymentStatus, String lockExpiryTime) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.userId = userId;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.lockExpiryTime = lockExpiryTime;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getLockExpiryTime() {
        return lockExpiryTime;
    }

    public void setLockExpiryTime(String lockExpiryTime) {
        this.lockExpiryTime = lockExpiryTime;
    }
}
