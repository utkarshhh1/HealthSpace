package com.healthspace.backend.service;

import com.healthspace.backend.entity.*;
import com.healthspace.backend.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HospitalAdminService {

    private final Logger logger = LoggerFactory.getLogger(HospitalAdminService.class);

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private HospitalAdminProfileRepository adminProfileRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    // --- CORE HELPER ---
    public Long getHospitalIdForAdmin(Long userId) {
        HospitalAdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You are not linked to any Hospital."));

        if (profile.getHospital() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No hospital linked to admin account");
        }

        // Optional: specific check if hospital is ACTIVE before allowing management
        if (!"ACTIVE".equalsIgnoreCase(profile.getHospital().getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your hospital is not yet active.");
        }

        return profile.getHospital().getId();
    }

    // --- HOSPITAL REGISTRATION FLOW (NEW) ---

    public HospitalAdminProfile getProfile(Long userId) {
        return adminProfileRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public Hospital registerHospitalForAdmin(Long adminUserId, Hospital hospitalData) {
        // 1. Validate uniqueness (basic check, DB constraint will also catch this)
        if (hospitalRepository.existsByLicenseNumber(hospitalData.getLicenseNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "License number already registered");
        }

        // 2. Create the Hospital (Status PENDING)
        hospitalData.setStatus("PENDING");
        Hospital savedHospital = hospitalRepository.save(hospitalData);

        // 3. Link Profile
        HospitalAdminProfile profile = adminProfileRepository.findByUserId(adminUserId)
                .orElse(new HospitalAdminProfile());

        User user = new User();
        user.setId(adminUserId); // Proxy user for FK reference

        profile.setUser(user);
        profile.setHospital(savedHospital);
        profile.setJobTitle("Chief Administrator");

        adminProfileRepository.save(profile);

        logger.info("Hospital registered (PENDING) id={} by Admin id={}", savedHospital.getId(), adminUserId);
        return savedHospital;
    }

    // --- DOCTOR MANAGEMENT ---

    public List<DoctorProfile> getPendingDoctors(Long hospitalId) {
        return doctorProfileRepository.findByHospitalIdAndAffiliationStatus(hospitalId, "PENDING");
    }

    public DoctorProfile approveDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        Long adminHospitalId = getHospitalIdForAdmin(hospitalAdminUserId);

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        if (doctorProfile.getHospital() == null || !adminHospitalId.equals(doctorProfile.getHospital().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot approve a doctor for a different hospital.");
        }

        doctorProfile.setAffiliationStatus("VERIFIED");
        return doctorProfileRepository.save(doctorProfile);
    }

    public DoctorProfile rejectDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        Long adminHospitalId = getHospitalIdForAdmin(hospitalAdminUserId);

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        if (doctorProfile.getHospital() == null || !adminHospitalId.equals(doctorProfile.getHospital().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot reject a doctor from a different hospital.");
        }

        doctorProfile.setAffiliationStatus("REJECTED");
        return doctorProfileRepository.save(doctorProfile);
    }

    // --- DASHBOARD DATA ---

    public List<Appointment> getHospitalAppointments(Long adminUserId) {
        Long hospitalId = getHospitalIdForAdmin(adminUserId);
        return appointmentRepository.findByHospitalIdOrderByAppointmentTimeDesc(hospitalId);
    }

    public List<Prescription> getHospitalPrescriptions(Long adminUserId) {
        Long hospitalId = getHospitalIdForAdmin(adminUserId);
        return prescriptionRepository.findByHospitalId(hospitalId);
    }
}