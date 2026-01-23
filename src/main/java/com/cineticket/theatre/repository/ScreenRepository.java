package com.cineticket.theatre.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineticket.theatre.entity.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByActiveTrue();

}
