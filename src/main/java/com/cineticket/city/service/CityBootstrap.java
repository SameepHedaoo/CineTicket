package com.cineticket.city.service;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cineticket.theatre.repository.TheatreRepository;

@Component
public class CityBootstrap implements CommandLineRunner {
    private final TheatreRepository theatreRepository;
    private final CityService cityService;

    public CityBootstrap(TheatreRepository theatreRepository, CityService cityService) {
        this.theatreRepository = theatreRepository;
        this.cityService = cityService;
    }

    @Override
    public void run(String... args) {
        List<String> cities = theatreRepository.findAll().stream()
                .map(theatre -> theatre.getCity())
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim())
                .map(name -> name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1))
                .distinct()
                .toList();
        for (String city : cities) {
            cityService.ensureCityExists(city);
        }
    }
}
