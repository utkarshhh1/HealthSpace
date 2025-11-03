package com.healthspace.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // This is the standard, strong password hashing algorithm
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simple APIs
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the /api/users/register endpoint
                        .requestMatchers("/api/users/register").permitAll()
                        // All other requests must be authenticated (we'll build login next)
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}