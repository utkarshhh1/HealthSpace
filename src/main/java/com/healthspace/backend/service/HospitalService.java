package com.healthspace.backend.service;

import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.repository.HospitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HospitalService {

    private final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    @Autowired
    private HospitalRepository hospitalRepository;

    // Used by Super Admin (Auto-Active) and Hospital Admin (Pending)
    public Hospital registerHospital(Hospital hospital, boolean autoApprove) {
        if (hospital == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital payload required");
        }
        if (hospitalRepository.existsByLicenseNumber(hospital.getLicenseNumber())) {
            // Allow update if ID matches (handled by controller usually), but for new check uniqueness
            if (hospital.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "License Number already exists");
            }
        }

        hospital.setStatus(autoApprove ? "ACTIVE" : "PENDING");

        Hospital saved = hospitalRepository.save(hospital);
        logger.info("Registered hospital id={} name={} status={}", saved.getId(), saved.getName(), saved.getStatus());
        return saved;
    }

    public List<Hospital> getActiveHospitals() {
        return hospitalRepository.findByStatus("ACTIVE");
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Hospital> getPendingHospitals() {
        return hospitalRepository.findByStatus("PENDING");
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
    }

    public Hospital approveHospital(Long id) {
        Hospital h = getHospitalById(id);
        h.setStatus("ACTIVE");
        return hospitalRepository.save(h);
    }
}