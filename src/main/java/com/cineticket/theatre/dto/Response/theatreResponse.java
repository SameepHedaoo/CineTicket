package com.cineticket.theatre.dto.Response;

import java.util.List;

public class TheatreResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private List<ScreenResponse> screens;

    public TheatreResponse(Long id, String name, String city, String address, List<ScreenResponse> screens) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.screens = screens;
    }

    public Long getId() {
        return id;
    }

    public List<ScreenResponse> getScreens() {
        return screens;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
