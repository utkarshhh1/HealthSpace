package com.healthspace.backend.controller;

import com.healthspace.backend.dto.AuthRequest;
import com.healthspace.backend.dto.AuthResponse;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserService userService;

    // POST /api/auth/register
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        // This still uses your existing secure registration logic
        return userService.createUser(user);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            String token = userService.loginUser(authRequest.getEmail(), authRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            // If login fails, return 401 Unauthorized
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
