package com.healthspace.backend.service;

import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.PatientProfileRepository;
import com.healthspace.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientProfileService {

    private final Logger logger = LoggerFactory.getLogger(PatientProfileService.class);

    @Autowired
    private PatientProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    public PatientProfile createOrUpdateProfile(Long userId, PatientProfile profileDetails) {
        if (userId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID required");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PatientProfile profile = profileRepository.findByUserId(userId)
                .orElse(new PatientProfile());

        profile.setUser(user);
        profile.setDob(profileDetails.getDob());
        profile.setGender(profileDetails.getGender());
        profile.setBloodGroup(profileDetails.getBloodGroup());
        profile.setHeight(profileDetails.getHeight());
        profile.setWeight(profileDetails.getWeight());
        profile.setAddress(profileDetails.getAddress());
        profile.setEmergencyContactName(profileDetails.getEmergencyContactName());
        profile.setEmergencyContactPhone(profileDetails.getEmergencyContactPhone());

        PatientProfile saved = profileRepository.save(profile);
        logger.info("Saved patient profile id={} userId={}", saved.getId(), userId);
        return saved;
    }

    public PatientProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}
