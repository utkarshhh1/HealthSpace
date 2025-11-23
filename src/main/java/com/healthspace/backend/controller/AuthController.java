package com.healthspace.backend.controller;

import com.healthspace.backend.entity.User;
import com.healthspace.backend.dto.AuthRequest;
import com.healthspace.backend.dto.AuthResponse;
import com.healthspace.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints. Registration is open; login returns JWT + basic user info.
 * CORS kept permissive for dev ports.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            // Remove password before returning
            created.setPassword(null);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            String token = userService.loginUser(authRequest.getEmail(), authRequest.getPassword());
            User user = (User) userService.loadUserByUsername(authRequest.getEmail());
            // Build response with essential details
            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole(), user.getId());
            // Optionally include other fields (name, isVerified) if you extend AuthResponse and backend returns them
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
