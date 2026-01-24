package com.cineticket.show.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.show.Service.ShowService;
import com.cineticket.show.dto.ShowRequest;
import com.cineticket.show.dto.ShowResponse;

@RestController
@RequestMapping("/admin/shows")
public class AdminShowController {
    private final ShowService showService;

    public AdminShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping("/add")
    public ResponseEntity<ShowResponse> createShow(
            @RequestBody ShowRequest request) {
        ShowResponse response = showService.createShow(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<Void> deleteShow(
            @PathVariable Long showId) {
        showService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }
}
