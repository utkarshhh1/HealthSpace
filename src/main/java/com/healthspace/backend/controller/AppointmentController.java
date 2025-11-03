package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000") // Allows React to connect
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // POST /api/appointments/book
    @PostMapping("/book")
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
        return appointmentService.bookAppointment(appointment);
    }

    // GET /api/appointments/patient/1
    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentService.getAppointmentsForPatient(patientId);
    }

    // GET /api/appointments/doctor/1
    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.getAppointmentsForDoctor(doctorId);
    }

    // PUT /api/appointments/cancel/1
    @PutMapping("/cancel/{appointmentId}")
    public Appointment cancelAppointment(@PathVariable Long appointmentId) {
        return appointmentService.cancelAppointment(appointmentId);
    }
}