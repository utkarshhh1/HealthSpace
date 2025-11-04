package com.healthspace.backend.service;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.entity.PatientProfile;
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
        // Find the user this profile belongs to
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if a profile already exists
        PatientProfile profile = profileRepository.findByUserId(userId)
                .orElse(new PatientProfile()); // If not, create a new one

        // Set the details
        profile.setUser(user);
        profile.setGender(profileDetails.getGender());
        profile.setAge(profileDetails.getAge());
        profile.setWeight(profileDetails.getWeight());
        profile.setContactNo(profileDetails.getContactNo());
        profile.setAddress(profileDetails.getAddress());

        return profileRepository.save(profile);
    }

    public PatientProfile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}