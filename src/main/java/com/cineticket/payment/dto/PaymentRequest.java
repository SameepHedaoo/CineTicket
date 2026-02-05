package com.cineticket.payment.dto;

import com.cineticket.booking.entity.PaymentStatus;

public class PaymentRequest {
    private Long bookingId;
    private PaymentStatus status;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
