package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatus(String status, Pageable pageable);
    
    // Provider-specific queries
    List<Appointment> findByProvider(User provider);
    List<Appointment> findByProviderOrderByAppointmentTimeAsc(User provider);
    List<Appointment> findByProviderAndStatusOrderByAppointmentTimeAsc(User provider, String status);
    List<Appointment> findByProviderAndAppointmentTimeAfterOrderByAppointmentTimeAsc(User provider, LocalDateTime dateTime);
    
    // Paginated queries
    Page<Appointment> findByProvider(User provider, Pageable pageable);
    Page<Appointment> findByProviderAndStatus(User provider, String status, Pageable pageable);
    
    // Search functionality
    @Query("SELECT a FROM Appointment a WHERE a.provider = :provider AND " +
           "(LOWER(a.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(a.client.firstName, ' ', a.client.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByProviderAndClientNameContainingIgnoreCaseOrderByAppointmentTimeAsc(
        @Param("provider") User provider, @Param("searchTerm") String searchTerm);
}
