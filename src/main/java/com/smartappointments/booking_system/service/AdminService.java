package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // User Management
    public Page<User> searchUsers(String searchTerm, String role, String status, Pageable pageable) {
        try {
            User.UserRole roleEnum = (role != null && !role.isEmpty())
                    ? User.UserRole.valueOf(role.toUpperCase())
                    : null;

            Boolean isActive = (status != null && !status.isEmpty())
                    ? status.equalsIgnoreCase("active")
                    : null;

            if (roleEnum != null && isActive != null) {
                return userRepository.findByRoleAndIsActive(roleEnum, isActive, pageable);
            } else if (roleEnum != null) {
                return userRepository.findByRole(roleEnum, pageable);
            } else if (isActive != null) {
                return userRepository.findByIsActive(isActive, pageable);
            } else if (searchTerm != null && !searchTerm.isEmpty()) {
                return userRepository.searchUsers(searchTerm, pageable);
            }
            return userRepository.findAll(pageable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role value: " + role);
        }
    }

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public boolean updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setActive(isActive);
        userRepository.save(user);
        return user.isActive();
    }

    // Updated methods to use proper enum types
    @Deprecated
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Deprecated
    public List<User> getUsersByRole(String role) {
        try {
            User.UserRole roleEnum = User.UserRole.valueOf(role.toUpperCase());
            return userRepository.findByRoleString(roleEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role value: " + role);
        }
    }

    // Appointment Management
    public Page<Appointment> getAppointments(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return (Page<Appointment>) appointmentRepository.findByStatus(status, pageable);
        }
        return appointmentRepository.findAll(pageable);
    }

    // Audit Logs
    public Page<AuditLog> getAuditLogs(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return (Page<AuditLog>) auditLogRepository.findByActionContaining(keyword, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }
}