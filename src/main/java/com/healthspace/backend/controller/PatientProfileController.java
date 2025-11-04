package com.healthspace.backend.controller;

import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.service.PatientProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/patient")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientProfileController {

    @Autowired
    private PatientProfileService profileService;

    // A Patient can create or update their own profile
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientProfile createOrUpdateProfile(@PathVariable Long userId, @RequestBody PatientProfile profile) {
        return profileService.createOrUpdateProfile(userId, profile);
    }

    // A Patient or Doctor can get the profile
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public PatientProfile getProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }
}