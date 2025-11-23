package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"})
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<Medicine> searchMedicines(@RequestParam String query) {
        return medicineService.searchMedicineByName(query);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Medicine addMedicine(@RequestBody Medicine medicine) {
        return medicineService.addMedicine(medicine);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }
}
