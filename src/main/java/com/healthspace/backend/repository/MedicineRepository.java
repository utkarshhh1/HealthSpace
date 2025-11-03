package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Spring Data JPA will automatically create a query
    // that searches for medicines by name
    List<Medicine> findByNameContainingIgnoreCase(String name);
}