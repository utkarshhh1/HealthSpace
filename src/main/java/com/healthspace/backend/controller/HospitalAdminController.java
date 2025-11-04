package com.healthspace.backend.controller;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.HospitalAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-admin")
@CrossOrigin(origins = "http://localhost:3000")
public class HospitalAdminController {

    @Autowired
    private HospitalAdminService hospitalAdminService;

    // This helper method correctly gets our custom User object
    private User getLoggedInUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    // Get all doctors with "PENDING" status for *my* hospital
    @GetMapping("/pending-doctors")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<List<DoctorProfile>> getPendingDoctors(Authentication authentication) {
        // Get the logged-in admin's user object
        User hospitalAdmin = getLoggedInUser(authentication);

        // Find their hospital ID
        Long hospitalId = hospitalAdmin.getHospitalId();
        if (hospitalId == null) {
            // This admin isn't linked to a hospital
            return ResponseEntity.status(403).build();
        }

        List<DoctorProfile> pendingDoctors = hospitalAdminService.getPendingDoctors(hospitalId);
        return ResponseEntity.ok(pendingDoctors);
    }

    // Approve a doctor
    @PutMapping("/approve-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<DoctorProfile> approveDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User hospitalAdmin = getLoggedInUser(authentication);
            DoctorProfile approvedProfile = hospitalAdminService.approveDoctor(doctorProfileId, hospitalAdmin.getId());
            return ResponseEntity.ok(approvedProfile);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build(); // Forbidden
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Reject a doctor
    @PutMapping("/reject-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<DoctorProfile> rejectDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User hospitalAdmin = getLoggedInUser(authentication);
            DoctorProfile rejectedProfile = hospitalAdminService.rejectDoctor(doctorProfileId, hospitalAdmin.getId());
            return ResponseEntity.ok(rejectedProfile);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build(); // Forbidden
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}