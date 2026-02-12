package com.cineticket.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class securityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public securityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("Forbidden");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public auth endpoints
                        .requestMatchers("/auth/**", "/health").permitAll()
                        // Public read-only endpoints
                        .requestMatchers(HttpMethod.GET, "/movies/**", "/theatres/**", "/shows/**", "/cities/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/movies/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/movies/**").authenticated()
                        // Manager + admin endpoints
                        .requestMatchers(HttpMethod.POST, "/admin/movies/*/poster").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        .requestMatchers("/admin/movies/**").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/admin/shows/**").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/admin/shows/**").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        .requestMatchers(HttpMethod.POST, "/theatres/screens").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/theatres/screens/**").hasAnyRole("ADMIN", "THEATRE_MANAGER")
                        // Admin-only endpoints
                        .requestMatchers("/admin/theatre-managers/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/theatres/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/theatres/**").hasRole("ADMIN")
                        // Booking requires authenticated users
                        .requestMatchers("/bookings/**").authenticated()
                        .requestMatchers("/payments/**").authenticated()
                        // Everything else is denied by default
                        .anyRequest().denyAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
