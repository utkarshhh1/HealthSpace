package com.healthspace.backend.controller;

import com.healthspace.backend.entity.HospitalAdminProfile;
import com.healthspace.backend.service.HospitalAdminProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/hospital-admin")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class HospitalAdminProfileController {

    @Autowired
    private HospitalAdminProfileService profileService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public HospitalAdminProfile createOrUpdateProfile(@PathVariable Long userId, @RequestBody HospitalAdminProfile profile) {
        return profileService.createOrUpdateProfile(userId, profile);
    }
}
