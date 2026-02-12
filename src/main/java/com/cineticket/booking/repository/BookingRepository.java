package com.cineticket.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.time.LocalDateTime;
import com.cineticket.booking.entity.Booking;
import com.cineticket.booking.entity.BookingStatus;
import com.cineticket.booking.entity.PaymentStatus;
import com.cineticket.show.Entity.ShowSeat;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
                SELECT ss
                FROM Booking b
                JOIN b.showSeats ss
                WHERE b.show.id = :showId AND b.status IN ('CONFIRMED', 'EXPIRED')
            """)
    List<ShowSeat> findBookedSeatsByShowId(Long showId);

    List<Booking> findByStatusAndLockExpiryTimeBefore(BookingStatus status, LocalDateTime time);

    List<Booking> findByStatusAndPaymentStatusAndShow_EndTimeBefore(
            BookingStatus status,
            PaymentStatus paymentStatus,
            LocalDateTime time);

    void deleteByShow_Id(Long showId);

    List<Booking> findByUserIdOrderByIdDesc(Long userId);
}
