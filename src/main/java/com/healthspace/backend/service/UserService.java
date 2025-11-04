package com.healthspace.backend.service;

import com.healthspace.backend.entity.User;
import com.healthspace.backend.repository.UserRepository;
import com.healthspace.backend.security.JwtUtil;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User createUser(User user) {
        // Ensure role is uppercase for consistency
        user.setRole(user.getRole().toUpperCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String loginUser(String email, String password) throws Exception {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the provided password matches
        if (passwordEncoder.matches(password, user.getPassword())) {
            // Since our User entity *is* the UserDetails, we just pass it
            return jwtUtil.generateToken(user);
        } else {
            throw new Exception("Invalid credentials");
        }
    }

    // --- THIS METHOD IS NOW MUCH SIMPLER ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Just find the user by email and return it.
        // It already implements UserDetails!
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    // --- END OF UPDATE ---

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}