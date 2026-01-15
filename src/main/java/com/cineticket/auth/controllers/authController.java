package com.cineticket.auth.controllers;

import com.cineticket.auth.dto.AuthResponse;
import com.cineticket.auth.dto.LoginRequest;
import com.cineticket.auth.dto.RegisterRequest;
import com.cineticket.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class authController {

    @Autowired
    private AuthService authService;

    // // REGISTER
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return new AuthResponse(result);

    }

    // Login
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        String result = authService.login(request);
        return new AuthResponse(result);

    }

}