package com.healthspace.backend.controller;

import com.healthspace.backend.dto.PrescriptionRequest;
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "http://localhost:3000")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public Prescription createPrescription(@RequestBody PrescriptionRequest request, Authentication authentication) {
        // We extract the Doctor's ID from the JWT token (User Principal)
        User doctorUser = (User) authentication.getPrincipal();
        return prescriptionService.createPrescription(request, doctorUser.getId());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public List<Prescription> getPatientPrescriptions(@PathVariable Long patientId) {
        // Security Check (Optional but recommended):
        // Ensure the logged-in user is either the patient themselves or a doctor.
        return prescriptionService.getPrescriptionsForPatient(patientId);
    }
}