package com.healthspace.backend.service;

import com.healthspace.backend.entity.Appointment;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment bookAppointment(Appointment appointment) {
        if (appointment.getHospitalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital ID is mandatory");
        }
        // default status on booking
        appointment.setStatus("BOOKED");
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Return only upcoming BOOKED appointments for doctor.
     * This prevents showing already completed or cancelled appointments in the doctor's schedule.
     */
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByDoctorIdAndAppointmentTimeAfterAndStatus(doctorId, now.minusMinutes(1), "BOOKED");
    }

    public Appointment cancelAppointmentByUser(Long appointmentId, User requester) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        // Only patient who owns appointment or assigned doctor can cancel
        boolean allowed = (requester.getId().equals(appointment.getPatientId()))
                || (requester.getId().equals(appointment.getDoctorId()))
                || ("ADMIN".equalsIgnoreCase(requester.getRole()));

        if (!allowed) {
            throw new SecurityException("Not permitted to cancel this appointment");
        }

        appointment.setStatus("CANCELLED");
        return appointmentRepository.save(appointment);
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }
}
