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
|    ├── SecurityConfig.java         // Password encoder + Spring Security config
|    └── JwtUtil.java                // JWT token generator + validator
│
├── movie/                    // 🎬 PHASE 3.1
│   ├── entity/
│   │   └── Movie.java
│   │
│   ├── repository/
│   │   └── MovieRepository.java
│   │
│   ├── dto/
│   │   ├── MovieRequest.java
│   │   └── MovieResponse.java
│   │
│   ├── service/
│   │   └── MovieService.java
│   │
│   └── controller/
│       ├── MovieController.java       // public APIs
│       └── AdminMovieController.java  // admin APIs
│
├── theatre/                  // 🏛️ PHASE 3.2
│   ├── entity/
│   │   ├── Theatre.java
│   │   ├── Screen.java
│   │   └── Seat.java
│   │
│   ├── repository/
│   │   ├── TheatreRepository.java
│   │   ├── ScreenRepository.java
│   │   └── SeatRepository.java
│   │
│   ├── dto/
│   │   ├── TheatreRequest.java
│   │   ├── ScreenRequest.java
│   │   └── SeatResponse.java
│   │
│   ├── service/
│   │   └── TheatreService.java
│   │
│   └── controller/
│       └── AdminTheatreController.java
│
├── show/                     // ⏰ PHASE 3.3
│   ├── entity/
│   │   └── Show.java
│   │
│   ├── repository/
│   │   └── ShowRepository.java
│   │
│   ├── dto/
│   │   ├── ShowRequest.java
│   │   └── ShowResponse.java
│   │
│   ├── service/
│   │   └── ShowService.java
│   │
│   └── controller/
│       ├── ShowController.java
│       └── AdminShowController.java
│
├── booking/                  // 💺 PHASE 3.4
│   ├── entity/
│   │   └── Booking.java
│   │
│   ├── repository/
│   │   └── BookingRepository.java
│   │
│   ├── dto/
│   │   ├── BookingRequest.java
│   │   └── BookingResponse.java
│   │
│   ├── service/
│   │   └── BookingService.java
│   │
│   └── controller/
│       └── BookingController.java


