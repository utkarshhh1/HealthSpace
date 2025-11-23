package com.healthspace.backend.controller;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.entity.HospitalAdminProfile;
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
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class HospitalAdminController {

    @Autowired
    private HospitalAdminService hospitalAdminService;

    private User getLoggedInUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    // --- REGISTRATION & PROFILE (NEW) ---

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        User admin = getLoggedInUser(authentication);
        HospitalAdminProfile profile = hospitalAdminService.getProfile(admin.getId());
        // Return 200 even if null (frontend handles "not configured" state)
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/register-hospital")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> registerOwnHospital(@RequestBody Hospital hospital, Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            // Ensure they don't already have a hospital linked
            HospitalAdminProfile existing = hospitalAdminService.getProfile(admin.getId());
            if (existing != null && existing.getHospital() != null) {
                return ResponseEntity.badRequest().body("You are already linked to a hospital.");
            }

            Hospital created = hospitalAdminService.registerHospitalForAdmin(admin.getId(), hospital);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    // --- DOCTOR MANAGEMENT ---

    @GetMapping("/pending-doctors")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> getPendingDoctors(Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            Long hospitalId = hospitalAdminService.getHospitalIdForAdmin(admin.getId());
            List<DoctorProfile> pending = hospitalAdminService.getPendingDoctors(hospitalId);
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Access Denied: " + e.getMessage());
        }
    }

    @PutMapping("/approve-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> approveDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            DoctorProfile updated = hospitalAdminService.approveDoctor(doctorProfileId, admin.getId());
            return ResponseEntity.ok(updated);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reject-doctor/{doctorProfileId}")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long doctorProfileId, Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            DoctorProfile updated = hospitalAdminService.rejectDoctor(doctorProfileId, admin.getId());
            return ResponseEntity.ok(updated);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- DASHBOARD DATA ---

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> getHospitalAppointments(Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            return ResponseEntity.ok(hospitalAdminService.getHospitalAppointments(admin.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('HOSPITAL_ADMIN')")
    public ResponseEntity<?> getHospitalPrescriptions(Authentication authentication) {
        try {
            User admin = getLoggedInUser(authentication);
            return ResponseEntity.ok(hospitalAdminService.getHospitalPrescriptions(admin.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}