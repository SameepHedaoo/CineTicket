package com.cineticket.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cineticket.auth.entity.UserEntity;
import com.cineticket.auth.enums.Role;
import com.cineticket.auth.repository.UserRepository;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@example.com}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            System.out.println("Admin bootstrap skipped: ADMIN_PASSWORD not set.");
            return;
        }

        UserEntity existing = userRepository.findByEmail(adminEmail);
        if (existing != null) {
            if (existing.getRole() != Role.ADMIN) {
                existing.setRole(Role.ADMIN);
                userRepository.save(existing);
            }
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
