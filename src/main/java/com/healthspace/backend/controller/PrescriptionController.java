package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "http://localhost:3000") // Allows React to connect
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // POST /api/prescriptions/create
    @PostMapping("/create")
    public Prescription createPrescription(@RequestBody Prescription prescription) {
        return prescriptionService.createPrescription(prescription);
    }

    // GET /api/prescriptions/patient/1
    @GetMapping("/patient/{patientId}")
    public List<Prescription> getPatientPrescriptions(@PathVariable Long patientId) {
        return prescriptionService.getPrescriptionsForPatient(patientId);
    }

    // GET /api/prescriptions/doctor/1
    @GetMapping("/doctor/{doctorId}")
    public List<Prescription> getDoctorPrescriptions(@PathVariable Long doctorId) {
        return prescriptionService.getPrescriptionsForDoctor(doctorId);
    }
}