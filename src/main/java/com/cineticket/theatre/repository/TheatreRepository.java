package com.cineticket.theatre.repository;

import java.util.List;

import com.cineticket.theatre.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    @Query("SELECT t FROM Theatre t WHERE UPPER(TRIM(t.city)) = UPPER(TRIM(:city))")
    List<Theatre> findByCitySafe(@Param("city") String city);

}
