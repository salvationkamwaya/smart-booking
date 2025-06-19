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
    
    // Client-specific queries
    List<Appointment> findByClient(User client);
    List<Appointment> findByClientOrderByAppointmentTimeDesc(User client);
    List<Appointment> findByClientAndStatusOrderByAppointmentTimeDesc(User client, String status);
    List<Appointment> findByClientAndAppointmentTimeAfterOrderByAppointmentTimeAsc(User client, LocalDateTime dateTime);
    Page<Appointment> findByClient(User client, Pageable pageable);
    Page<Appointment> findByClientAndStatus(User client, String status, Pageable pageable);
    
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

    // Admin queries for system-wide appointments
    List<Appointment> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);
    List<Appointment> findAllByOrderByCreatedAtDesc();
    
    // ADMIN APPOINTMENT MANAGEMENT METHODS
    
    // Status-based queries with pagination
    Page<Appointment> findByStatusOrderByAppointmentTimeDesc(String status, Pageable pageable);
    List<Appointment> findByStatusOrderByAppointmentTimeDesc(String status);
    
    // Date-time range queries
    Page<Appointment> findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Appointment> findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(LocalDateTime start, LocalDateTime end);
    
    // Upcoming appointments
    Page<Appointment> findByAppointmentTimeAfterOrderByAppointmentTimeAsc(LocalDateTime dateTime, Pageable pageable);
    
    // All appointments with pagination
    Page<Appointment> findAllByOrderByAppointmentTimeDesc(Pageable pageable);
    List<Appointment> findAllByOrderByAppointmentTimeDesc();
    
    // Search with status filter
    @Query("SELECT a FROM Appointment a WHERE " +
           "(LOWER(a.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(a.client.firstName, ' ', a.client.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "a.status = :status " +
           "ORDER BY a.appointmentTime DESC")
    Page<Appointment> findByClientNameContainingIgnoreCaseAndStatusOrderByAppointmentTimeDesc(
        @Param("searchTerm") String searchTerm, @Param("status") String status, Pageable pageable);
    
    // Search without status filter
    @Query("SELECT a FROM Appointment a WHERE " +
           "(LOWER(a.client.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.client.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(a.client.firstName, ' ', a.client.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY a.appointmentTime DESC")
    Page<Appointment> findByClientNameContainingIgnoreCaseOrderByAppointmentTimeDesc(
        @Param("searchTerm") String searchTerm, Pageable pageable);
      // Count methods for statistics
    long countByAppointmentTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, String status);
    long countByRescheduledAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Date range queries for reporting
    List<Appointment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Appointment> findByProviderAndCreatedAtBetween(User provider, LocalDateTime startDate, LocalDateTime endDate);
}
