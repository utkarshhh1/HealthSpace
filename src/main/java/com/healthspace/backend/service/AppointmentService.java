package com.healthspace.backend.service;

import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Logic to book a new appointment
    public Appointment bookAppointment(Appointment appointment) {
        // In a real app, we'd check for conflicts.
        // For now, we just set the status and save.
        appointment.setStatus("BOOKED");
        return appointmentRepository.save(appointment);
    }

    // Logic to get all appointments for a patient
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    // Logic to get all appointments for a doctor
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    // Logic to cancel an appointment
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }
}