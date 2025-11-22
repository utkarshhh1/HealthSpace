package com.healthspace.backend.service;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public List<Medicine> searchMedicineByName(String query) {
        // Improved Search: Look in Brand Name OR Generic Name
        List<Medicine> brandMatches = medicineRepository.findByBrandNameContainingIgnoreCase(query);
        List<Medicine> genericMatches = medicineRepository.findByGenericNameContainingIgnoreCase(query);

        // Merge results (avoid duplicates if needed, but list addition is fine for MVP)
        List<Medicine> allMatches = new ArrayList<>(brandMatches);
        allMatches.addAll(genericMatches);

        return allMatches;
    }
}