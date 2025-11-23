package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment, Authentication authentication) {
        try {
            User current = (User) authentication.getPrincipal();
            if (appointment.getPatientId() != null && !current.getId().equals(appointment.getPatientId())) {
                return ResponseEntity.status(403).body("Forbidden: cannot create appointment for another patient.");
            }
            Appointment created = appointmentService.bookAppointment(appointment);
            return ResponseEntity.ok(created);
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to book appointment: " + e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long patientId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("PATIENT".equalsIgnoreCase(current.getRole()) && !current.getId().equals(patientId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        List<Appointment> list = appointmentService.getAppointmentsForPatient(patientId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable Long doctorId, Authentication authentication) {
        User current = (User) authentication.getPrincipal();
        if ("DOCTOR".equalsIgnoreCase(current.getRole()) && !current.getId().equals(doctorId)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        List<Appointment> list = appointmentService.getAppointmentsForDoctor(doctorId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            User current = (User) authentication.getPrincipal();
            String role = current.getRole();

            if ("ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.ok(appointment);
            }
            if ("DOCTOR".equalsIgnoreCase(role)) {
                if (appointment.getDoctorId() != null && appointment.getDoctorId().equals(current.getId())) {
                    return ResponseEntity.ok(appointment);
                }
                return ResponseEntity.status(403).body("Forbidden: not the assigned doctor.");
            }
            if ("PATIENT".equalsIgnoreCase(role)) {
                if (appointment.getPatientId() != null && appointment.getPatientId().equals(current.getId())) {
                    return ResponseEntity.ok(appointment);
                }
                return ResponseEntity.status(403).body("Forbidden: not your appointment.");
            }
            return ResponseEntity.status(403).build();
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch appointment: " + e.getMessage());
        }
    }

    @PutMapping("/cancel/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            User current = (User) authentication.getPrincipal();
            Appointment cancelled = appointmentService.cancelAppointmentByUser(appointmentId, current);
            return ResponseEntity.ok(cancelled);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body("Forbidden");
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to cancel: " + e.getMessage());
        }
    }
}