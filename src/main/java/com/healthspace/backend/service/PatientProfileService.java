package com.healthspace.backend.service;

import com.healthspace.backend.entity.PatientProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.PatientProfileRepository;
import com.healthspace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientProfileService {

    @Autowired
    private PatientProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    public PatientProfile createOrUpdateProfile(Long userId, PatientProfile profileDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PatientProfile profile = profileRepository.findByUserId(userId)
                .orElse(new PatientProfile());

        profile.setUser(user);

        // New Fields Mapping
        profile.setDob(profileDetails.getDob());
        profile.setGender(profileDetails.getGender());
        profile.setBloodGroup(profileDetails.getBloodGroup());
        profile.setHeight(profileDetails.getHeight());
        profile.setWeight(profileDetails.getWeight());
        profile.setAddress(profileDetails.getAddress());
        profile.setEmergencyContactName(profileDetails.getEmergencyContactName());
        profile.setEmergencyContactPhone(profileDetails.getEmergencyContactPhone());

        return profileRepository.save(profile);
    }

    public PatientProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}