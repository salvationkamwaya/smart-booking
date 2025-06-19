package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    // Get appointments for a specific provider
    public List<Appointment> getProviderAppointments(User provider) {
        return appointmentRepository.findByProviderOrderByAppointmentTimeAsc(provider);
    }

    // Get appointments for a specific provider with status filter
    public List<Appointment> getProviderAppointmentsByStatus(User provider, String status) {
        return appointmentRepository.findByProviderAndStatusOrderByAppointmentTimeAsc(provider, status);
    }

    // Get paginated appointments for a provider
    public Page<Appointment> getProviderAppointments(User provider, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").ascending());
        return appointmentRepository.findByProvider(provider, pageable);
    }

    // Get appointments by status with pagination
    public Page<Appointment> getProviderAppointmentsByStatus(User provider, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").ascending());
        return appointmentRepository.findByProviderAndStatus(provider, status, pageable);
    }

    // Get upcoming appointments for a provider
    public List<Appointment> getUpcomingAppointments(User provider) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByProviderAndAppointmentTimeAfterOrderByAppointmentTimeAsc(provider, now);
    }

    // Get appointment statistics for a provider
    public Map<String, Long> getAppointmentStats(User provider) {
        List<Appointment> allAppointments = appointmentRepository.findByProvider(provider);
        
        long total = allAppointments.size();
        long pending = allAppointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmed = allAppointments.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long cancelled = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        
        LocalDateTime now = LocalDateTime.now();
        long upcoming = allAppointments.stream()
            .filter(a -> a.getAppointmentTime().isAfter(now))
            .filter(a -> !"CANCELLED".equals(a.getStatus()))
            .count();

        return Map.of(
            "total", total,
            "pending", pending,
            "confirmed", confirmed,
            "completed", completed,
            "cancelled", cancelled,
            "upcoming", upcoming
        );
    }

    // Find appointment by ID
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);    }

    // Update appointment status
    public Appointment updateAppointmentStatus(Long appointmentId, String status, User provider) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        
        // Verify that the appointment belongs to this provider
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only update your own appointments");
        }

        String oldStatus = appointment.getStatus();
        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Send status change notifications
        try {
            notificationService.notifyAppointmentStatusChanged(updatedAppointment, oldStatus, status);
        } catch (Exception e) {
            // Log error but don't fail the status update
            System.err.println("Failed to send status change notifications for appointment " + appointmentId + ": " + e.getMessage());
        }
        
        return updatedAppointment;
    }

    // Reschedule appointment
    public Appointment rescheduleAppointment(Long appointmentId, LocalDateTime newTime, User provider) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        
        // Verify that the appointment belongs to this provider
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only reschedule your own appointments");
        }

        appointment.setAppointmentTime(newTime);
        return appointmentRepository.save(appointment);
    }

    // Cancel appointment
    public Appointment cancelAppointment(Long appointmentId, User provider) {
        return updateAppointmentStatus(appointmentId, "CANCELLED", provider);
    }

    // Confirm appointment
    public Appointment confirmAppointment(Long appointmentId, User provider) {
        return updateAppointmentStatus(appointmentId, "CONFIRMED", provider);
    }

    // Complete appointment
    public Appointment completeAppointment(Long appointmentId, User provider) {
        return updateAppointmentStatus(appointmentId, "COMPLETED", provider);
    }

    // Start appointment (sets status to IN_PROGRESS)
    public Appointment startAppointment(Long appointmentId, User provider) {
        return updateAppointmentStatus(appointmentId, "IN_PROGRESS", provider);
    }

    // Update appointment notes
    public Appointment updateAppointmentNotes(Long appointmentId, String notes, User provider) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        
        // Verify that the appointment belongs to this provider
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only update your own appointments");
        }

        appointment.setNotes(notes);
        return appointmentRepository.save(appointment);
    }    // Create new appointment
    public Appointment createAppointment(Appointment appointment) {
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send notifications to all relevant parties
        try {
            notificationService.notifyAppointmentBooked(savedAppointment);
        } catch (Exception e) {
            // Log error but don't fail the appointment creation
            System.err.println("Failed to send notifications for appointment " + savedAppointment.getId() + ": " + e.getMessage());
        }
        
        return savedAppointment;
    }

    // Search appointments
    public List<Appointment> searchProviderAppointments(User provider, String searchTerm) {
        return appointmentRepository.findByProviderAndClientNameContainingIgnoreCaseOrderByAppointmentTimeAsc(
            provider, searchTerm);
    }

    // Get all appointments since a specific date
    public List<Appointment> getAllAppointmentsSince(LocalDateTime since) {
        return appointmentRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since);
    }

    // Get all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderByCreatedAtDesc();
    }

    // Client-specific methods
    
    // Get appointments for a specific client
    public List<Appointment> getClientAppointments(User client) {
        return appointmentRepository.findByClientOrderByAppointmentTimeDesc(client);
    }

    // Get appointments for a specific client with status filter
    public List<Appointment> getClientAppointmentsByStatus(User client, String status) {
        return appointmentRepository.findByClientAndStatusOrderByAppointmentTimeDesc(client, status);
    }

    // Get paginated appointments for a client
    public Page<Appointment> getClientAppointments(User client, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").descending());
        return appointmentRepository.findByClient(client, pageable);
    }

    // Get appointments by status with pagination for client
    public Page<Appointment> getClientAppointmentsByStatus(User client, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentTime").descending());
        return appointmentRepository.findByClientAndStatus(client, status, pageable);
    }

    // Get upcoming appointments for a client
    public List<Appointment> getUpcomingClientAppointments(User client) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByClientAndAppointmentTimeAfterOrderByAppointmentTimeAsc(client, now);
    }

    // Get appointment statistics for a client
    public Map<String, Long> getClientAppointmentStats(User client) {
        List<Appointment> allAppointments = appointmentRepository.findByClient(client);
        
        long total = allAppointments.size();
        long pending = allAppointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmed = allAppointments.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long cancelled = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        
        LocalDateTime now = LocalDateTime.now();
        long upcoming = allAppointments.stream()
            .filter(a -> a.getAppointmentTime().isAfter(now))
            .filter(a -> !"CANCELLED".equals(a.getStatus()))
            .count();

        return Map.of(
            "total", total,
            "pending", pending,
            "confirmed", confirmed,
            "completed", completed,
            "cancelled", cancelled,
            "upcoming", upcoming
        );
    }

    // Cancel appointment by client
    public Appointment cancelAppointmentByClient(Long appointmentId, User client, String reason) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        
        // Verify that the appointment belongs to this client
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("You can only cancel your own appointments");
        }

        // Check if appointment can be cancelled (not already completed/cancelled)
        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Cannot cancel a completed or already cancelled appointment");
        }

        String oldStatus = appointment.getStatus();
        appointment.setStatus("CANCELLED");
        appointment.setCancellationReason(reason);
        appointment.setCancelledAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send notification to provider about cancellation
        notificationService.notifyProvider(
            appointment.getProvider(), 
            "Appointment Cancelled", 
            "Client " + client.getFirstName() + " " + client.getLastName() + 
            " has cancelled their appointment scheduled for " + appointment.getAppointmentTime()
        );

        return savedAppointment;
    }

    // Reschedule appointment by client
    public Appointment rescheduleAppointmentByClient(Long appointmentId, User client, LocalDateTime newDateTime, String reason) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        
        // Verify that the appointment belongs to this client
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("You can only reschedule your own appointments");
        }

        // Check if appointment can be rescheduled
        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Cannot reschedule a completed or cancelled appointment");
        }

        LocalDateTime oldDateTime = appointment.getAppointmentTime();
        appointment.setAppointmentTime(newDateTime);
        appointment.setStatus("PENDING"); // Reset to pending for provider approval
        appointment.setRescheduleReason(reason);
        appointment.setRescheduledAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send notification to provider about reschedule request
        notificationService.notifyProvider(
            appointment.getProvider(), 
            "Appointment Reschedule Request", 
            "Client " + client.getFirstName() + " " + client.getLastName() + 
            " has requested to reschedule their appointment from " + oldDateTime + " to " + newDateTime
        );

        return savedAppointment;
    }
}
