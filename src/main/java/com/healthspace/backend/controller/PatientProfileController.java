package com.healthspace.backend.controller;

import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.PatientProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/patient")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class PatientProfileController {

    @Autowired
    private PatientProfileService profileService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createOrUpdateProfile(@PathVariable Long userId, @RequestBody PatientProfile profile, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if (!current.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        PatientProfile saved = profileService.createOrUpdateProfile(userId, profile);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getProfile(@PathVariable Long userId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("PATIENT".equalsIgnoreCase(current.getRole()) && !current.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        PatientProfile p = profileService.getProfileByUserId(userId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }
}
