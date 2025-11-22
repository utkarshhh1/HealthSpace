package com.healthspace.backend.repository;

import com.healthspace.backend.entity.HospitalAdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HospitalAdminProfileRepository extends JpaRepository<HospitalAdminProfile, Long> {
    Optional<HospitalAdminProfile> findByUserId(Long userId);
}