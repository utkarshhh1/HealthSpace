package com.healthspace.backend.controller;
import com.healthspace.backend.service.HospitalAdminService;
import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.User;
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

    private User getLoggedInUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/pending-doctors")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<List<DoctorProfile>> getPendingDoctors(Authentication authentication) {
        User hospitalAdmin = getLoggedInUser(authentication);

        try {
            // NEW LOGIC: Ask Service to look up the Hospital ID
            Long hospitalId = hospitalAdminService.getHospitalIdForAdmin(hospitalAdmin.getId());
            List<DoctorProfile> pendingDoctors = hospitalAdminService.getPendingDoctors(hospitalId);
            return ResponseEntity.ok(pendingDoctors);
        } catch (Exception e) {
            return ResponseEntity.status(403).build(); // Forbidden if no profile found
        }
    }

    @PutMapping("/approve-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<DoctorProfile> approveDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User hospitalAdmin = getLoggedInUser(authentication);
            DoctorProfile approvedProfile = hospitalAdminService.approveDoctor(doctorProfileId, hospitalAdmin.getId());
            return ResponseEntity.ok(approvedProfile);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reject-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<DoctorProfile> rejectDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User hospitalAdmin = getLoggedInUser(authentication);
            DoctorProfile rejectedProfile = hospitalAdminService.rejectDoctor(doctorProfileId, hospitalAdmin.getId());
            return ResponseEntity.ok(rejectedProfile);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}