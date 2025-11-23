package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // Public: Only see Active hospitals (For patients/doctors searching)
    @GetMapping
    public List<Hospital> getActiveHospitals() {
        return hospitalService.getActiveHospitals();
    }

    // Admin: See All (Active + Pending)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Hospital> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    // Admin: Register directly (Auto-Approve)
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public Hospital registerHospital(@RequestBody Hospital hospital) {
        return hospitalService.registerHospital(hospital, true); // true = auto approve
    }

    // Admin: Approve a Pending Hospital
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Hospital approveHospital(@PathVariable Long id) {
        return hospitalService.approveHospital(id);
    }

    @GetMapping("/{id}")
    public Hospital getHospitalById(@PathVariable Long id) {
        return hospitalService.getHospitalById(id);
    }
}