package com.healthspace.backend.service;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.repository.MedicineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MedicineService {

    private final Logger logger = LoggerFactory.getLogger(MedicineService.class);

    @Autowired
    private MedicineRepository medicineRepository;

    public Medicine addMedicine(Medicine medicine) {
        if (medicine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medicine payload required");
        }
        Medicine saved = medicineRepository.save(medicine);
        logger.info("Added medicine id={} brand={} generic={}", saved.getId(), saved.getBrandName(), saved.getGenericName());
        return saved;
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public List<Medicine> searchMedicineByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<Medicine> brandMatches = medicineRepository.findByBrandNameContainingIgnoreCase(query);
        List<Medicine> genericMatches = medicineRepository.findByGenericNameContainingIgnoreCase(query);

        // merge without duplicates (by id)
        List<Medicine> all = new ArrayList<>(brandMatches);
        for (Medicine m : genericMatches) {
            if (all.stream().noneMatch(existing -> existing.getId().equals(m.getId()))) {
                all.add(m);
            }
        }
        return all;
    }
}
