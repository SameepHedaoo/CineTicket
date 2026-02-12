package com.cineticket.city.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.city.dto.CityResponse;
import com.cineticket.city.service.CityService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/cities")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public List<CityResponse> list() {
        return cityService.getCitiesForCurrentRole();
    }
}
