package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // <-- IMPORT THIS
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "http://localhost:3000")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // Only a 'DOCTOR' can create a prescription
    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public Prescription createPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.createPrescription(prescription);
    }

    // Only a 'PATIENT' can see their own prescriptions
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public List<Prescription> getPatientPrescriptions(@PathVariable Long patientId) {
        return prescriptionService.getPrescriptionsForPatient(patientId);
    }

    // Only a 'DOCTOR' can see the prescriptions they wrote
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<Prescription> getDoctorPrescriptions(@PathVariable Long doctorId) {
        return prescriptionService.getPrescriptionsForDoctor(doctorId);
    }
}