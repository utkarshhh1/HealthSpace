package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Find all prescriptions for a specific patient
    List<Prescription> findByPatientId(Long patientId);

    // Find all prescriptions for a specific doctor
    List<Prescription> findByDoctorId(Long doctorId);
}