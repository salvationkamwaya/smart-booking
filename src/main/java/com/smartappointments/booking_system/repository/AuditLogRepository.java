package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActionContaining(String keyword, Pageable pageable);
}
