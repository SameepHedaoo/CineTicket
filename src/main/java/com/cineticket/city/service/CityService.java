package com.cineticket.city.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cineticket.city.dto.CityResponse;
import com.cineticket.city.entity.City;
import com.cineticket.city.repository.CityRepository;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(city -> new CityResponse(city.getId(), city.getName()))
                .toList();
    }

    public CityResponse createCity(String name) {
        City city = cityRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            City created = new City();
            created.setName(name);
            return cityRepository.save(created);
        });
        return new CityResponse(city.getId(), city.getName());
    }

    public void ensureCityExists(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        cityRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            City created = new City();
            created.setName(name);
            return cityRepository.save(created);
        });
    }
}
