package com.healthspace.backend.service;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.HospitalRepository;
import com.healthspace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoctorProfileService {

    @Autowired
    private DoctorProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    public DoctorProfile createOrUpdateProfile(Long userId, DoctorProfile profileDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User (Doctor) not found"));

        DoctorProfile profile = profileRepository.findByUserId(userId)
                .orElse(new DoctorProfile());

        profile.setUser(user);

        // Handle Hospital Link
        if (profileDetails.getHospital() != null && profileDetails.getHospital().getId() != null) {
            Hospital hospital = hospitalRepository.findById(profileDetails.getHospital().getId())
                    .orElseThrow(() -> new RuntimeException("Hospital not found"));
            profile.setHospital(hospital);
        }

        // Updated Field Mappings
        profile.setSpecialty(profileDetails.getSpecialty());
        profile.setDegree(profileDetails.getDegree()); // Renamed from qualifications
        profile.setExperienceYears(profileDetails.getExperienceYears());
        profile.setConsultationFee(profileDetails.getConsultationFee());

        // Status Logic: If updating, keep existing status. If new, set PENDING.
        if (profile.getId() == null) {
            profile.setAffiliationStatus("PENDING");
        }

        return profileRepository.save(profile);
    }

    public DoctorProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
    }
}