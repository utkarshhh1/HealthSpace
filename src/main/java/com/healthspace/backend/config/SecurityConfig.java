package com.healthspace.backend.config;

import com.healthspace.backend.security.JwtAuthFilter;
import com.healthspace.backend.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer; // Added Import
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationProvider authenticationProvider,
                                           JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // CRITICAL FIX: Activates CorsConfig
                .authorizeHttpRequests(auth -> auth
                        // Allow preflight OPTIONS everywhere
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public read endpoints
                        .requestMatchers(HttpMethod.GET, "/api/hospitals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/doctor/verified").permitAll()
                        // Role-specific endpoints
                        .requestMatchers(HttpMethod.GET, "/api/prescriptions/doctor/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/appointments/doctor/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/prescriptions/create").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/prescriptions/patient/**").hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/profiles/patient/**").hasAnyRole("PATIENT", "DOCTOR", "HOSPITAL_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasRole("PATIENT")
                        .requestMatchers("/api/hospital-admin/**").hasRole("HOSPITAL_ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/medicines/**").hasRole("ADMIN")
                        .requestMatchers("/api/hospitals/register").hasRole("ADMIN")
                        // Authenticated default
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}