package com.healthspace.backend.service;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.HospitalAdminProfile;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.HospitalAdminProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HospitalAdminService {

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private HospitalAdminProfileRepository adminProfileRepository;

    // Helper method to get Hospital ID for a logged-in Admin
    public Long getHospitalIdForAdmin(Long userId) {
        HospitalAdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Access Denied: You are not linked to any Hospital."));
        return profile.getHospital().getId();
    }

    public List<DoctorProfile> getPendingDoctors(Long hospitalId) {
        return doctorProfileRepository.findByHospitalIdAndAffiliationStatus(hospitalId, "PENDING");
    }

    public DoctorProfile approveDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        // 1. Get the Admin's Hospital ID
        Long adminHospitalId = getHospitalIdForAdmin(hospitalAdminUserId);

        // 2. Get the Doctor Profile
        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        // 3. Security Check: Do they match?
        if (!adminHospitalId.equals(doctorProfile.getHospital().getId())) {
            throw new SecurityException("You cannot approve a doctor for a different hospital.");
        }

        doctorProfile.setAffiliationStatus("VERIFIED");
        return doctorProfileRepository.save(doctorProfile);
    }

    public DoctorProfile rejectDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        // 1. Get the Admin's Hospital ID
        Long adminHospitalId = getHospitalIdForAdmin(hospitalAdminUserId);

        // 2. Get the Doctor Profile
        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        // 3. Security Check
        if (!adminHospitalId.equals(doctorProfile.getHospital().getId())) {
            throw new SecurityException("Unauthorized action.");
        }

        doctorProfile.setAffiliationStatus("REJECTED");
        return doctorProfileRepository.save(doctorProfile);
    }
}