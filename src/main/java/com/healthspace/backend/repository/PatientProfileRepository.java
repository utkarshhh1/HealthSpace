package com.healthspace.backend.repository;

import com.healthspace.backend.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    // Every patient profile lookup is based on userId
    Optional<PatientProfile> findByUserId(Long userId);
}
