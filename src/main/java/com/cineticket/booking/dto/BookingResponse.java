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
    private String movieName;
    private String theatreName;
    private String screenName;
    private String showStartTime;
    private String showEndTime;
    private Double basePrice;

    public BookingResponse(Long bookingId, Long showId, Long userId, List<String> seats, Double totalPrice,
            String status, String paymentStatus, String lockExpiryTime,
            String movieName, String theatreName, String screenName,
            String showStartTime, String showEndTime, Double basePrice) {
        this.bookingId = bookingId;
        this.showId = showId;
        this.userId = userId;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.lockExpiryTime = lockExpiryTime;
        this.movieName = movieName;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.showStartTime = showStartTime;
        this.showEndTime = showEndTime;
        this.basePrice = basePrice;
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

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(String showStartTime) {
        this.showStartTime = showStartTime;
    }

    public String getShowEndTime() {
        return showEndTime;
    }

    public void setShowEndTime(String showEndTime) {
        this.showEndTime = showEndTime;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }
}
