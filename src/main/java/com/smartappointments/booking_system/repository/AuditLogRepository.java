package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.AuditLog;
import com.smartappointments.booking_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Basic search and filtering
    Page<AuditLog> findByActionContainingIgnoreCase(String keyword, Pageable pageable);
    Page<AuditLog> findByDetailsContainingIgnoreCase(String keyword, Pageable pageable);
    
    // Filter by entity type
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    // Filter by user
    Page<AuditLog> findByPerformedBy(User user, Pageable pageable);
    
    // Filter by severity
    Page<AuditLog> findBySeverity(String severity, Pageable pageable);
    
    // Filter by success status
    Page<AuditLog> findBySuccess(Boolean success, Pageable pageable);
    
    // Date range filtering
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Combined search query
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:entityType IS NULL OR :entityType = '' OR a.entityType = :entityType) AND " +
           "(:severity IS NULL OR :severity = '' OR a.severity = :severity) AND " +
           "(:success IS NULL OR a.success = :success) AND " +
           "(:userId IS NULL OR a.performedBy.id = :userId) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findLogsWithFilters(
        @Param("search") String search,
        @Param("entityType") String entityType,
        @Param("severity") String severity,
        @Param("success") Boolean success,
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Statistics queries
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
    long countBySeverityAndTimestampBetween(String severity, LocalDateTime start, LocalDateTime end);
    long countBySuccessAndTimestampBetween(Boolean success, LocalDateTime start, LocalDateTime end);
    long countByEntityTypeAndTimestampBetween(String entityType, LocalDateTime start, LocalDateTime end);
    
    // Recent activities
    List<AuditLog> findTop10ByOrderByTimestampDesc();
    List<AuditLog> findByPerformedByOrderByTimestampDesc(User user, Pageable pageable);
    
    // Action-specific queries
    Page<AuditLog> findByActionAndTimestampBetween(String action, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Failed operations
    Page<AuditLog> findBySuccessFalseOrderByTimestampDesc(Pageable pageable);
    
    // Critical events
    Page<AuditLog> findBySeverityInOrderByTimestampDesc(List<String> severities, Pageable pageable);
}
