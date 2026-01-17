package com.cineticket.auth.controllers;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineticket.auth.dto.UserResponseDTO;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.repository.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
