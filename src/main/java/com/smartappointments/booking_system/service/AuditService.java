package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.AuditLog;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an audit event
     */
    public AuditLog logEvent(String action, String entityType, Long entityId, String details, 
                           User performedBy, String severity, Boolean success, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setPerformedBy(performedBy);
        auditLog.setSeverity(severity != null ? severity : "INFO");
        auditLog.setSuccess(success != null ? success : true);
        auditLog.setTimestamp(LocalDateTime.now());
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return auditLogRepository.save(auditLog);
    }

    /**
     * Log a simple audit event
     */
    public AuditLog logEvent(String action, String details, User performedBy) {
        return logEvent(action, null, null, details, performedBy, "INFO", true, null);
    }

    /**
     * Log user-related events
     */
    public AuditLog logUserEvent(String action, User targetUser, String details, User performedBy, HttpServletRequest request) {
        return logEvent(action, "USER", targetUser.getId(), details, performedBy, "INFO", true, request);
    }

    /**
     * Log appointment-related events
     */
    public AuditLog logAppointmentEvent(String action, Long appointmentId, String details, User performedBy, HttpServletRequest request) {
        return logEvent(action, "APPOINTMENT", appointmentId, details, performedBy, "INFO", true, request);
    }

    /**
     * Log system events
     */
    public AuditLog logSystemEvent(String action, String details, String severity) {
        return logEvent(action, "SYSTEM", null, details, null, severity, true, null);
    }

    /**
     * Log failed events
     */
    public AuditLog logFailedEvent(String action, String details, User performedBy, HttpServletRequest request) {
        return logEvent(action, null, null, details, performedBy, "ERROR", false, request);
    }

    /**
     * Get audit logs with filters and pagination
     */
    public Page<AuditLog> getAuditLogs(String search, String entityType, String severity, 
                                     Boolean success, Long userId, LocalDateTime startDate, 
                                     LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findLogsWithFilters(search, entityType, severity, success, 
                                                    userId, startDate, endDate, pageable);
    }

    /**
     * Get all audit logs with pagination
     */
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop10ByOrderByTimestampDesc();
    }

    /**
     * Get user-specific audit logs
     */
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        return auditLogRepository.findByPerformedBy(user, pageable);
    }

    /**
     * Get failed operations
     */
    public Page<AuditLog> getFailedOperations(Pageable pageable) {
        return auditLogRepository.findBySuccessFalseOrderByTimestampDesc(pageable);
    }

    /**
     * Get critical events
     */
    public Page<AuditLog> getCriticalEvents(Pageable pageable) {
        List<String> criticalSeverities = List.of("ERROR", "CRITICAL");
        return auditLogRepository.findBySeverityInOrderByTimestampDesc(criticalSeverities, pageable);
    }

    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);
        
        // Total logs
        stats.put("totalLogs", auditLogRepository.count());
        
        // Today's logs
        stats.put("todayLogs", auditLogRepository.countByTimestampBetween(startOfDay, now));
        
        // This week's logs
        stats.put("weekLogs", auditLogRepository.countByTimestampBetween(startOfWeek, now));
        
        // This month's logs
        stats.put("monthLogs", auditLogRepository.countByTimestampBetween(startOfMonth, now));
        
        // Error logs today
        stats.put("todayErrors", auditLogRepository.countBySeverityAndTimestampBetween("ERROR", startOfDay, now));
        
        // Failed operations today
        stats.put("todayFailures", auditLogRepository.countBySuccessAndTimestampBetween(false, startOfDay, now));
        
        // Entity type breakdown (today)
        stats.put("todayUserActions", auditLogRepository.countByEntityTypeAndTimestampBetween("USER", startOfDay, now));
        stats.put("todayAppointmentActions", auditLogRepository.countByEntityTypeAndTimestampBetween("APPOINTMENT", startOfDay, now));
        stats.put("todaySystemActions", auditLogRepository.countByEntityTypeAndTimestampBetween("SYSTEM", startOfDay, now));
        
        return stats;
    }

    /**
     * Get audit log by ID
     */
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Audit log not found with ID: " + id));
    }

    /**
     * Export audit logs
     */
    public List<AuditLog> exportAuditLogs(String search, String entityType, String severity, 
                                        Boolean success, Long userId, LocalDateTime startDate, 
                                        LocalDateTime endDate) {
        // Get all matching logs without pagination for export
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<AuditLog> page = auditLogRepository.findLogsWithFilters(search, entityType, severity, 
                                                                   success, userId, startDate, endDate, pageable);
        return page.getContent();
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    // Convenience methods for common audit events

    public void logUserLogin(User user, HttpServletRequest request) {
        logUserEvent("USER_LOGIN", user, "User logged into the system", user, request);
    }

    public void logUserLogout(User user, HttpServletRequest request) {
        logUserEvent("USER_LOGOUT", user, "User logged out of the system", user, request);
    }

    public void logUserCreated(User newUser, User createdBy, HttpServletRequest request) {
        logUserEvent("USER_CREATED", newUser, 
            String.format("New user created: %s %s (%s)", newUser.getFirstName(), newUser.getLastName(), newUser.getEmail()), 
            createdBy, request);
    }

    public void logUserUpdated(User updatedUser, User updatedBy, HttpServletRequest request) {
        logUserEvent("USER_UPDATED", updatedUser, 
            String.format("User profile updated: %s %s", updatedUser.getFirstName(), updatedUser.getLastName()), 
            updatedBy, request);
    }

    public void logUserDeleted(User deletedUser, User deletedBy, HttpServletRequest request) {
        logUserEvent("USER_DELETED", deletedUser, 
            String.format("User deleted: %s %s (%s)", deletedUser.getFirstName(), deletedUser.getLastName(), deletedUser.getEmail()), 
            deletedBy, request);
    }

    public void logAppointmentCreated(Long appointmentId, String clientName, String providerName, User createdBy, HttpServletRequest request) {
        logAppointmentEvent("APPOINTMENT_CREATED", appointmentId, 
            String.format("New appointment created: %s with %s", clientName, providerName), 
            createdBy, request);
    }

    public void logAppointmentUpdated(Long appointmentId, String details, User updatedBy, HttpServletRequest request) {
        logAppointmentEvent("APPOINTMENT_UPDATED", appointmentId, details, updatedBy, request);
    }

    public void logAppointmentDeleted(Long appointmentId, String details, User deletedBy, HttpServletRequest request) {
        logAppointmentEvent("APPOINTMENT_DELETED", appointmentId, details, deletedBy, request);
    }

    public void logAppointmentStatusChanged(Long appointmentId, String oldStatus, String newStatus, User changedBy, HttpServletRequest request) {
        logAppointmentEvent("APPOINTMENT_STATUS_CHANGED", appointmentId, 
            String.format("Status changed from %s to %s", oldStatus, newStatus), 
            changedBy, request);
    }
}
