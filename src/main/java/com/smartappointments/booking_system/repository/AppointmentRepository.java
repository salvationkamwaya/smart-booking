package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatus(String status, Pageable pageable);
}
