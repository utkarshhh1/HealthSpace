package com.healthspace.backend.controller;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.service.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/doctor")
@CrossOrigin(origins = "http://localhost:3000")
public class DoctorProfileController {

    @Autowired
    private DoctorProfileService profileService;

    // A Doctor can create or update their *own* profile
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfile createOrUpdateProfile(@PathVariable Long userId, @RequestBody DoctorProfile profile) {
        return profileService.createOrUpdateProfile(userId, profile);
    }

    // Anyone (e.g., a patient) can get a doctor's profile
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public DoctorProfile getProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }
}