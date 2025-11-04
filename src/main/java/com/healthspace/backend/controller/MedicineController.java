package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- IMPORT THIS
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "http://localhost:3000")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    // Anyone who is logged in (authenticated) can see medicines
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    // Only an 'ADMIN' can add a new medicine to the store
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine addMedicine(@RequestBody Medicine medicine) {
        return medicineService.addMedicine(medicine);
    }

    // Anyone logged in can see a single medicine
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Anyone logged in can search
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<Medicine> searchMedicines(@RequestParam String name) {
        return medicineService.searchMedicineByName(name);
    }
}