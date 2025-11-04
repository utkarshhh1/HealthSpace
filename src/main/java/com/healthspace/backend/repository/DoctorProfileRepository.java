package com.healthspace.backend.repository;

import com.healthspace.backend.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    List<DoctorProfile> findBySpecialtyContainingIgnoreCase(String specialty);

    List<DoctorProfile> findByHospitalId(Long hospitalId);

    // --- NEW METHOD ---
    // Finds all doctors linked to a hospital with a specific status
    List<DoctorProfile> findByHospitalIdAndAffiliationStatus(Long hospitalId, String status);
}