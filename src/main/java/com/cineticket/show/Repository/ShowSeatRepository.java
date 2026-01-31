package com.cineticket.show.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineticket.show.Entity.ShowSeat;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByIdIn(List<Long> ids);
}
