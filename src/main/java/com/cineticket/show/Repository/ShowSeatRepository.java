package com.cineticket.show.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cineticket.show.Entity.ShowSeat;
import com.cineticket.show.Entity.ShowSeatStatus;

import jakarta.persistence.LockModeType;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("""
        SELECT s FROM ShowSeat s WHERE s.id IN :ids ORDER BY s.id
    """)
    
    List<ShowSeat> findByIdsForUpdate(@Param("ids") List<Long> ids);

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByIdIn(List<Long> ids);

    void deleteByShowId(Long showId);

    long countByShowId(Long showId);

    long countByShowIdAndStatus(Long showId, ShowSeatStatus status);
}
