package com.smartappointments.booking_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "USER_CREATED", "APPOINTMENT_DELETED", "LOGIN", "LOGOUT"
    
    @Column(name = "entity_type")
    private String entityType; // e.g., "USER", "APPOINTMENT", "SYSTEM"
    
    @Column(name = "entity_id")
    private Long entityId; // ID of the affected entity
    
    @Column(columnDefinition = "TEXT")
    private String details; // JSON or descriptive details of the action
    
    @Column(name = "ip_address")
    private String ipAddress; // IP address of the user performing the action
    
    @Column(name = "user_agent")
    private String userAgent; // Browser/device information
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "severity")
    private String severity; // INFO, WARN, ERROR, CRITICAL
    
    @Column(name = "success")
    private Boolean success; // Whether the action was successful

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
        this.severity = "INFO";
    }

    public AuditLog(String action, String details, User performedBy) {
        this();
        this.action = action;
        this.details = details;
        this.performedBy = performedBy;
    }

    public AuditLog(String action, String entityType, Long entityId, String details, User performedBy) {
        this(action, details, performedBy);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    // Utility methods
    public String getPerformedByName() {
        if (performedBy != null) {
            return performedBy.getFirstName() + " " + performedBy.getLastName();
        }
        return "System";
    }

    public String getSeverityIcon() {
        switch (severity.toUpperCase()) {
            case "ERROR":
            case "CRITICAL":
                return "fas fa-exclamation-triangle text-red-500";
            case "WARN":
                return "fas fa-exclamation-circle text-yellow-500";
            case "INFO":
            default:
                return "fas fa-info-circle text-blue-500";
        }
    }

    public String getActionIcon() {
        String actionLower = action.toLowerCase();
        if (actionLower.contains("create") || actionLower.contains("add")) {
            return "fas fa-plus text-green-500";
        } else if (actionLower.contains("update") || actionLower.contains("edit")) {
            return "fas fa-edit text-blue-500";
        } else if (actionLower.contains("delete") || actionLower.contains("remove")) {
            return "fas fa-trash text-red-500";
        } else if (actionLower.contains("login")) {
            return "fas fa-sign-in-alt text-green-500";
        } else if (actionLower.contains("logout")) {
            return "fas fa-sign-out-alt text-gray-500";
        } else {
            return "fas fa-cog text-gray-500";
        }
    }
}