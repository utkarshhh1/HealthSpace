package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Patient history, most recent first
    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // Doctor history, most recent first
    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    // Distinct patient ids for a doctor
    List<Prescription> findDistinctByDoctorId(Long doctorId);

    // NEW: Find all prescriptions issued within a specific hospital
    @Query("SELECT p FROM Prescription p JOIN p.appointment a WHERE a.hospitalId = :hospitalId ORDER BY p.createdAt DESC")
    List<Prescription> findByHospitalId(@Param("hospitalId") Long hospitalId);
}