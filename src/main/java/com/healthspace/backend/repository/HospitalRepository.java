package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    boolean existsByLicenseNumber(String licenseNumber);

    // Fetch active hospitals for public search
    List<Hospital> findByStatus(String status);
}