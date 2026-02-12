package com.cineticket.city.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cineticket.city.dto.CityResponse;
import com.cineticket.city.entity.City;
import com.cineticket.city.repository.CityRepository;
import com.cineticket.show.Repository.ShowRepository;

@Service
public class CityService {
    private final CityRepository cityRepository;
    private final ShowRepository showRepository;

    public CityService(CityRepository cityRepository, ShowRepository showRepository) {
        this.cityRepository = cityRepository;
        this.showRepository = showRepository;
    }

    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(city -> new CityResponse(city.getId(), city.getName()))
                .toList();
    }

    public List<CityResponse> getCitiesForCurrentRole() {
        if (isAdminOrTheatreManager()) {
            return getAllCities();
        }

        Set<String> citiesWithShows = new HashSet<>();
        for (String cityName : showRepository.findDistinctCitiesWithShows()) {
            if (cityName != null && !cityName.isBlank()) {
                citiesWithShows.add(cityName.trim().toLowerCase(Locale.ROOT));
            }
        }

        return cityRepository.findAll().stream()
                .filter(city -> city.getName() != null
                        && citiesWithShows.contains(city.getName().trim().toLowerCase(Locale.ROOT)))
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

    private boolean isAdminOrTheatreManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_THEATRE_MANAGER".equals(a.getAuthority()));
    }
}
