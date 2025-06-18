package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<User> getUsers(User.Role role, User.Status status, String search, Pageable pageable) {
        return userRepository.findByFilters(role, status, search, pageable);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        if (user.getStatus() == null) {
            user.setStatus(User.Status.PENDING);
        }

        return userRepository.save(user);
    }    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }    @Transactional
    public User updateUserStatus(Long id, User.Status status) {
        User user = getUserById(id);
        user.setStatus(status);
        
        if (status == User.Status.ACTIVE) {
            // If activating a pending user, update their activation time
            user.setLastLoginDate(LocalDateTime.now());
        }
        
        return userRepository.save(user);
    }

    public long countByRoleAndStatus(User.Role role, User.Status status) {
        return userRepository.countByRoleAndStatus(role, status);
    }

    @Transactional
    public void updateLastLoginDate(User user) {
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // Find users by role
    public List<User> findByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    // Find user by ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
