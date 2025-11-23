package com.healthspace.backend.service;

import com.healthspace.backend.entity.Hospital;
import com.healthspace.backend.entity.HospitalAdminProfile;
import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.HospitalAdminProfileRepository;
import com.healthspace.backend.repository.HospitalRepository;
import com.healthspace.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HospitalAdminProfileService {

    private final Logger logger = LoggerFactory.getLogger(HospitalAdminProfileService.class);

    @Autowired
    private HospitalAdminProfileRepository adminProfileRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HospitalRepository hospitalRepository;

    public HospitalAdminProfile createOrUpdateProfile(Long userId, HospitalAdminProfile profileDetails) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User (Hospital Admin) not found"));

        HospitalAdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElse(new HospitalAdminProfile());

        profile.setUser(user);

        if (profileDetails.getHospital() != null && profileDetails.getHospital().getId() != null) {
            Hospital hospital = hospitalRepository.findById(profileDetails.getHospital().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
            profile.setHospital(hospital);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hospital ID is mandatory for Hospital Admin profile.");
        }

        profile.setJobTitle(profileDetails.getJobTitle());

        HospitalAdminProfile saved = adminProfileRepository.save(profile);
        logger.info("Saved HospitalAdminProfile id={} userId={} hospitalId={}", saved.getId(), userId, saved.getHospital().getId());
        return saved;
    }

    public HospitalAdminProfile getByUserId(Long userId) {
        return adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital admin profile not found"));
    }
}
