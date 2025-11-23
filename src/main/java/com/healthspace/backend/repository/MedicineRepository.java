package com.healthspace.backend.repository;

import com.healthspace.backend.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // Searching by brand and generic name
    List<Medicine> findByBrandNameContainingIgnoreCase(String brandName);

    List<Medicine> findByGenericNameContainingIgnoreCase(String genericName);
}
