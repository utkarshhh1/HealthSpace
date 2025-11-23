package com.healthspace.backend.controller;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.service.DoctorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/doctor")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class DoctorProfileController {

    @Autowired
    private DoctorProfileService profileService;

    /**
     * POST /api/profiles/doctor/{userId}
     * Doctor creates or updates own profile.
     */
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfile createOrUpdateProfile(@PathVariable Long userId, @RequestBody DoctorProfile profile) {
        return profileService.createOrUpdateProfile(userId, profile);
    }

    /**
     * GET doctor's profile — any authenticated user may read.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public DoctorProfile getProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }

    /**
     * GET /verified — listing of verified doctors used in booking flows.
     */
    @GetMapping("/verified")
    @PreAuthorize("isAuthenticated()")
    public List<DoctorProfile> getVerifiedDoctors() {
        return profileService.getVerifiedDoctors();
    }

    /**
     * GET all doctors — admin-only management endpoint.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorProfile> getAllDoctors() {
        return profileService.getAllDoctors();
    }
}
