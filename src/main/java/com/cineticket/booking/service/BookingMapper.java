package com.cineticket.booking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.cineticket.booking.dto.BookingResponse;
import com.cineticket.booking.entity.Booking;
import com.cineticket.show.Entity.ShowSeat;

public final class BookingMapper {

    private static final Map<String, BigDecimal> SEAT_TYPE_MULTIPLIERS = Map.of(
            "REGULAR", BigDecimal.valueOf(1.0),
            "SILVER", BigDecimal.valueOf(1.15),
            "GOLD", BigDecimal.valueOf(1.30),
            "PREMIUM", BigDecimal.valueOf(1.50),
            "VIP", BigDecimal.valueOf(2.00));

    private BookingMapper() {
    }

    public static BookingResponse toBookingResponse(Booking booking) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (booking.getShow() != null && booking.getShow().getPrice() != null) {
            for (ShowSeat seat : booking.getShowSeats()) {
                String seatType = seat.getSeat().getSeatType();
                BigDecimal multiplier = SEAT_TYPE_MULTIPLIERS.getOrDefault(
                        seatType == null ? "REGULAR" : seatType.toUpperCase(Locale.ROOT),
                        BigDecimal.ONE);
                totalPrice = totalPrice.add(booking.getShow().getPrice().multiply(multiplier));
            }
        }

        List<String> seatNumbers = booking.getShowSeats().stream()
                .map(seat -> String.valueOf(seat.getSeat().getSeatNumber()))
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getShow() == null ? null : booking.getShow().getId(),
                booking.getUserId(),
                seatNumbers,
                totalPrice.doubleValue(),
                booking.getStatus() == null ? null : booking.getStatus().name(),
                booking.getPaymentStatus() == null ? null : booking.getPaymentStatus().name(),
                booking.getLockExpiryTime() == null ? null : booking.getLockExpiryTime().toString());
    }
}
