package com.healthspace.backend.controller;

import com.healthspace.backend.dto.PrescriptionRequest;
import com.healthspace.backend.entity.Prescription;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createPrescription(@RequestBody PrescriptionRequest request, Authentication authentication) {
        User doctorUser = (User) authentication.getPrincipal();
        try {
            if (request.getAppointmentId() != null &&
                    !prescriptionService.isDoctorAssignedToAppointment(doctorUser.getId(), request.getAppointmentId())) {
                return ResponseEntity.status(403).body("Forbidden: not assigned to appointment");
            }
            Prescription created = prescriptionService.createPrescription(request, doctorUser.getId());
            return ResponseEntity.ok(created);
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create prescription: " + e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPatientPrescriptions(@PathVariable Long patientId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("PATIENT".equalsIgnoreCase(current.getRole()) && !current.getId().equals(patientId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        List<Prescription> list = prescriptionService.getPrescriptionsForPatient(patientId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getDoctorPrescriptions(@PathVariable Long doctorId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("DOCTOR".equalsIgnoreCase(current.getRole()) && !current.getId().equals(doctorId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        List<Prescription> list = prescriptionService.getPrescriptionsForDoctor(doctorId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/doctor/{doctorId}/patients")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPatientsForDoctor(@PathVariable Long doctorId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("DOCTOR".equalsIgnoreCase(current.getRole()) && !current.getId().equals(doctorId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        List<Long> list = prescriptionService.getUniquePatientsForDoctor(doctorId);
        return ResponseEntity.ok(list);
    }
}