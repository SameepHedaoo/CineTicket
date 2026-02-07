package com.cineticket.city.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cineticket.city.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
}
