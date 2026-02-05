package com.cineticket.auth.controllers;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.auth.dto.UserResponseDTO;
import com.cineticket.auth.dto.TheatreManagerRequest;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;
import com.cineticket.theatre.repository.TheatreRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final TheatreRepository theatreRepository;

    public AdminController(UserRepository userRepository, TheatreRepository theatreRepository) {
        this.userRepository = userRepository;
        this.theatreRepository = theatreRepository;
    }

    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        System.out.println("Users found: " + users.size());
        List<UserResponseDTO> response = new ArrayList<>();
        for (UserEntity user : users) {
            UserResponseDTO responseDTOs = new UserResponseDTO(user.getId(), user.getEmail(), user.getRole().name());
            response.add(responseDTOs);
        }
        return response;
    }

    @PostMapping("/theatre-managers")
    public ResponseEntity<String> assignTheatreManager(@RequestBody TheatreManagerRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (request.getTheatreId() == null) {
            return ResponseEntity.badRequest().body("Theatre ID is required");
        }
        if (!theatreRepository.existsById(request.getTheatreId())) {
            return ResponseEntity.badRequest().body("Theatre not found");
        }
        UserEntity user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        user.setRole(Role.THEATRE_MANAGER);
        user.setTheatreId(request.getTheatreId());
        userRepository.save(user);
        return ResponseEntity.ok("Theatre manager assigned");
    }
}
