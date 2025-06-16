package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // User Management
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setActive(isActive);
        return userRepository.save(user);
    }

    // Appointment Management
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    // Audit Logs
    public List<AuditLog> getAuditLogs(String keyword) {
        return auditLogRepository.findByActionContaining(keyword);
    }
    // Add this method:
    public List<User> searchUsers(String searchTerm, String role, String status) {
        Specification<User> spec = Specification.where(null);

        if(searchTerm != null) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("name"), "%" + query + "%"),
                            cb.like(root.get("email"), "%" + query + "%")
                    ));
        }

        if(role != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("role"), role));
        }

        if(status != null) {
            boolean isActive = status.equals("active");
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("active"), isActive));
        }

        return userRepository.findAll((Sort) spec);
    }
}