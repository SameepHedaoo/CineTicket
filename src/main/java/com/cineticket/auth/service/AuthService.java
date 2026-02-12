package com.cineticket.auth.service;

import org.springframework.stereotype.Service;

import com.cineticket.auth.config.JwtUtil;
import com.cineticket.auth.dto.AuthResponse;
import com.cineticket.auth.dto.LoginRequest;
import com.cineticket.auth.dto.RegisterRequest;
import com.cineticket.auth.dto.UserResponseDTO;
import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;

    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return "Email already registered";
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(Role.USER);
        userRepository.save(userEntity);
        return "User Saved";
    }

    public AuthResponse login(LoginRequest request) {

        UserEntity user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                user.getTheatreId());

        return new AuthResponse(token, "Login successful");
    }

    public UserResponseDTO getCurrentUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new RuntimeException("User not found");
        }
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setEmail(userEntity.getEmail());
        userResponseDTO.setId(userEntity.getId());
        userResponseDTO.setRole(userEntity.getRole().name());
        return userResponseDTO;

    }
}
