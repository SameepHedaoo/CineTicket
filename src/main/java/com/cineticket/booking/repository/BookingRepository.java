package com.cineticket.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.cineticket.booking.entity.Booking;
import com.cineticket.show.Entity.ShowSeat;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
                SELECT ss
                FROM Booking b
                JOIN b.showSeats ss
                WHERE b.show.id = :showId AND b.status = 'CONFIRMED'
            """)
    List<ShowSeat> findBookedSeatsByShowId(Long showId);
}
