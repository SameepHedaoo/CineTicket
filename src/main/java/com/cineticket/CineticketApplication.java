package com.cineticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineticketApplication {
    public static void main(String[] args) {
        SpringApplication.run(CineticketApplication.class, args);
    }
}
