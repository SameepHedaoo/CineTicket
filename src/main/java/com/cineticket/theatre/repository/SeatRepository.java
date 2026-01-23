package com.cineticket.theatre.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineticket.theatre.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByActiveTrue();
}
