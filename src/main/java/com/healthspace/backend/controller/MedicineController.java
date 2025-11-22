package com.healthspace.backend.controller;

import com.healthspace.backend.entity.Medicine;
import com.healthspace.backend.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "http://localhost:3000")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<Medicine> searchMedicines(@RequestParam String query) {
        // Simple toggle: check both brand and generic
        // (Implementation detail: usually Service handles the logic, but this is fine for now)
        return medicineService.searchMedicineByName(query);
    }
}