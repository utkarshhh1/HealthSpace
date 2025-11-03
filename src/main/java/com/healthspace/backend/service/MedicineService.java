package com.healthspace.backend.service;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    // Add a new medicine to the catalog (for an Admin)
    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    // Get a list of all medicines
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    // Get one medicine by its ID
    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    // Search for a medicine by name
    public List<Medicine> searchMedicineByName(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }
}