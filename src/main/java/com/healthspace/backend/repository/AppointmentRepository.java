package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find all appointments for a specific patient
    List<Appointment> findByPatientId(Long patientId);

    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorId(Long doctorId);

    // Find future appointments for doctor (only useful to show upcoming schedule)
    List<Appointment> findByDoctorIdAndAppointmentTimeAfterAndStatus(Long doctorId, LocalDateTime after, String status);

    // NEW: Find all appointments for a specific hospital (For Hospital Admin Dashboard)
    List<Appointment> findByHospitalIdOrderByAppointmentTimeDesc(Long hospitalId);
}