package com.healthspace.backend.service;

import com.healthspace.backend.entity.DoctorProfile;
import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.DoctorProfileRepository;
import com.healthspace.backend.repository.HospitalRepository;
import com.healthspace.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DoctorProfileService {

    private final Logger logger = LoggerFactory.getLogger(DoctorProfileService.class);

    @Autowired
    private DoctorProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    public DoctorProfile createOrUpdateProfile(Long userId, DoctorProfile profileDetails) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User (Doctor) not found"));

        DoctorProfile profile = profileRepository.findByUserId(userId)
                .orElse(new DoctorProfile());

        profile.setUser(user);

        // Handle Hospital Link if provided
        if (profileDetails.getHospital() != null && profileDetails.getHospital().getId() != null) {
            Hospital hospital = hospitalRepository.findById(profileDetails.getHospital().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
            profile.setHospital(hospital);
        }

        // Set fields safely
        profile.setSpecialty(profileDetails.getSpecialty());
        profile.setDegree(profileDetails.getDegree());
        profile.setExperienceYears(profileDetails.getExperienceYears());
        profile.setConsultationFee(profileDetails.getConsultationFee());

        // Default/status logic
        if (profile.getId() == null || "REJECTED".equalsIgnoreCase(profile.getAffiliationStatus())) {
            profile.setAffiliationStatus("PENDING");
        } else if (profile.getAffiliationStatus() == null) {
            profile.setAffiliationStatus("PENDING");
        }

        DoctorProfile saved = profileRepository.save(profile);
        logger.info("Saved doctor profile id={} userId={} status={}", saved.getId(), userId, saved.getAffiliationStatus());
        return saved;
    }

    public DoctorProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    public List<DoctorProfile> getVerifiedDoctors() {
        return profileRepository.findByAffiliationStatus("VERIFIED");
    }

    public List<DoctorProfile> getAllDoctors() {
        return profileRepository.findAll();
    }
}
