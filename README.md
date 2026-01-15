# Movie Ticket Booking System

src/main/java/com/cineticket/auth/
│
├── CineticketApplication.java      // Main Spring Boot app class
│
├── entity/
│   └── User.java                   // Your JPA entity for users
│
├── repository/
│   └── UserRepository.java         // JPA repository interface
│
├── dto/
│   ├── RegisterRequest.java        // DTO for registration input
│   ├── LoginRequest.java           // DTO for login input
│   └── AuthResponse.java           // DTO for returning token + message
│
├── service/
│   └── AuthService.java            // Business logic: register + login
│
├── controller/
│   └── AuthController.java         // REST endpoints: /register, /login
│
└── security/
    ├── SecurityConfig.java         // Password encoder + Spring Security config
    └── JwtUtil.java                // JWT token generator + validator
