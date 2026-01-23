package com.cineticket.theatre.repository;

import java.util.Collection;
import java.util.List;

import com.cineticket.theatre.dto.Response.theatreResponse;
import com.cineticket.theatre.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    List<Theatre> findByActiveTrue();

    Collection<theatreResponse> findByCityIgnoreCase(String city);
}
