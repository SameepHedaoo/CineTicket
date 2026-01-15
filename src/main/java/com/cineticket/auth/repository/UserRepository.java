package com.cineticket.auth.repository;

import com.cineticket.auth.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);
}
