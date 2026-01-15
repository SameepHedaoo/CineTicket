package com.cineticket.auth.service;

import org.springframework.stereotype.Service;
import com.cineticket.auth.dto.LoginRequest;
import com.cineticket.auth.dto.RegisterRequest;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return "Email already registered";
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(request.getPassword());
        userEntity.setRole("USER");
        userRepository.save(userEntity);
        return "User Saved";
    }

    public String login(LoginRequest loginRequest) {
        UserEntity userEntity = userRepository.findByEmail(loginRequest.getEmail());
        if (userEntity == null) {
            return "User not found";
        } else if (!userEntity.getPassword().equals(loginRequest.getPassword())) {
            return "Incorrect Password";
        } else {
            return "Login Successful";
        }

    }
}
