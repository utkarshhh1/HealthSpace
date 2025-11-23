package com.healthspace.backend.service;

import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.UserRepository;
import com.healthspace.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User createUser(User user) {
        if (user == null) throw new IllegalArgumentException("User payload required");

        // Normalize role
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("PATIENT");
        } else {
            user.setRole(user.getRole().toUpperCase());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        User saved = userRepository.save(user);
        logger.info("Created user id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }

    /**
     * Attempts login and returns JWT token if credentials match.
     * Throws UsernameNotFoundException or RuntimeException for invalid creds.
     */
    public String loginUser(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken((UserDetails) user);
            logger.info("User logged in id={} email={}", user.getId(), user.getEmail());
            return token;
        } else {
            throw new Exception("Invalid credentials");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }
    public Optional<User> getUserById(Long id) { return userRepository.findById(id); }
    public Optional<User> getUserByEmail(String email) { return userRepository.findByEmail(email); }
}
