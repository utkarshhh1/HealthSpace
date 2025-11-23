package com.healthspace.backend.repository;

import com.healthspace.backend.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    // Fetch doctor profile by the Auth User ID
    Optional<DoctorProfile> findByUserId(Long userId);

    // Filter by specialty
    List<DoctorProfile> findBySpecialtyContainingIgnoreCase(String specialty);

    // Doctors under one hospital
    List<DoctorProfile> findByHospitalId(Long hospitalId);

    // Hospital admin filters
    List<DoctorProfile> findByHospitalIdAndAffiliationStatus(Long hospitalId, String status);

    // Doctor search for verified doctors (most common)
    List<DoctorProfile> findByAffiliationStatus(String status);
}
