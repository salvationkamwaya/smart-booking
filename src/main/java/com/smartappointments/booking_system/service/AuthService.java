package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String email, String firstName, String lastName, String password, String role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(User.Status.ACTIVE);

        // Set role based on string input
        switch (role.toLowerCase()) {
            case "client":
                user.setRole(User.Role.CLIENT);
                break;
            case "provider":
                user.setRole(User.Role.PROVIDER);
                break;
            case "admin":
                user.setRole(User.Role.ADMIN);
                break;
            default:
                throw new RuntimeException("Invalid role: " + role);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public User updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
