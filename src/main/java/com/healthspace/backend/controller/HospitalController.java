package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@CrossOrigin(origins = "http://localhost:3000")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // This endpoint would likely be for 'ADMIN' or a future 'HOSPITAL_ADMIN' role
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public Hospital registerHospital(@RequestBody Hospital hospital) {
        return hospitalService.registerHospital(hospital);
    }

    // Anyone logged in can see the list of hospitals
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Hospital> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Hospital getHospitalById(@PathVariable Long id) {
        return hospitalService.getHospitalById(id);
    }
}