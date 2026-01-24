package com.cineticket.show.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cineticket.show.Entity.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByScreenId(Long screenId);

    List<Show> findByScreen_Theatre_Id(Long theatreId);

    @Query("SELECT s FROM Show s WHERE LOWER(s.screen.theatre.city) = LOWER(:city)")
    List<Show> findByScreenTheatreCity(@Param("city") String city);

    @Query("""
                SELECT s FROM Show s
                WHERE s.screen.id = :screenId
                AND (
                    :startTime < s.endTime AND :endTime > s.startTime
                )
            """)
    List<Show> findOverlappingShows(
            Long screenId,
            LocalDateTime startTime,
            LocalDateTime endTime);
}
