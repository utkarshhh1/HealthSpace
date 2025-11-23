package com.healthspace.backend.controller;

import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class UserController {

    @Autowired
    private UserService userService;

    // Admin-only: list users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // Get user by id: admin or the user themselves
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if (!current.getId().equals(id) && !"ADMIN".equalsIgnoreCase(current.getRole())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> {
            u.setPassword(null);
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin-only: lookup by email
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(u -> {
            u.setPassword(null);
            return ResponseEntity.ok(u);
        }).orElse(ResponseEntity.notFound().build());
    }
}
