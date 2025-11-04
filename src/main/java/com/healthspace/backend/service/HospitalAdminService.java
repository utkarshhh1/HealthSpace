package com.healthspace.backend.service;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalAdminService {

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all doctors waiting for approval at a specific hospital
    public List<DoctorProfile> getPendingDoctors(Long hospitalId) {
        return doctorProfileRepository.findByHospitalIdAndAffiliationStatus(hospitalId, "PENDING");
    }

    // Approve a doctor
    public DoctorProfile approveDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        // Find the admin user
        User admin = userRepository.findById(hospitalAdminUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        // Find the doctor's profile
        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        // **Security Check:** Does this admin manage the hospital this doctor applied to?
        if (!admin.getHospitalId().equals(doctorProfile.getHospital().getId())) {
            throw new SecurityException("Admin not authorized to approve doctors for this hospital.");
        }

        // If checks pass, approve the doctor
        doctorProfile.setAffiliationStatus("VERIFIED");
        return doctorProfileRepository.save(doctorProfile);
    }

    // You could also add a rejectDoctor() method here
    public DoctorProfile rejectDoctor(Long doctorProfileId, Long hospitalAdminUserId) {
        User admin = userRepository.findById(hospitalAdminUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        DoctorProfile doctorProfile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        if (!admin.getHospitalId().equals(doctorProfile.getHospital().getId())) {
            throw new SecurityException("Admin not authorized to reject doctors for this hospital.");
        }

        doctorProfile.setAffiliationStatus("REJECTED");
        return doctorProfileRepository.save(doctorProfile);
    }
}