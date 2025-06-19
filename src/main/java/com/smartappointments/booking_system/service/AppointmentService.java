package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.AppointmentRepository;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

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
        
        // Log the status change
        try {
            auditService.logAppointmentStatusChanged(appointmentId, oldStatus, status, provider, null);
        } catch (Exception e) {
            System.err.println("Failed to log appointment status change: " + e.getMessage());
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
        
        // Log appointment creation
        try {
            auditService.logAppointmentCreated(
                savedAppointment.getId(), 
                savedAppointment.getClientFullName(), 
                savedAppointment.getProviderFullName(), 
                savedAppointment.getProvider(), 
                null
            );
        } catch (Exception e) {
            System.err.println("Failed to log appointment creation: " + e.getMessage());
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
        }        // Check if appointment can be cancelled (not already completed/cancelled)
        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Cannot cancel a completed or already cancelled appointment");
        }

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
    
    // ADMIN APPOINTMENT MANAGEMENT METHODS
    
    /**
     * Get filtered appointments for admin with pagination
     */
    public Page<Appointment> getFilteredAppointments(String search, String status, 
            LocalDateTime fromDate, LocalDateTime toDate, String tab, Pageable pageable) {
        
        // Build the query based on filters
        if (tab != null && !tab.isEmpty()) {
            switch (tab.toLowerCase()) {
                case "upcoming":
                    LocalDateTime now = LocalDateTime.now();
                    return appointmentRepository.findByAppointmentTimeAfterOrderByAppointmentTimeAsc(now, pageable);
                case "today":
                    LocalDate today = LocalDate.now();
                    LocalDateTime startOfDay = today.atStartOfDay();
                    LocalDateTime endOfDay = today.atTime(23, 59, 59);
                    return appointmentRepository.findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(
                        startOfDay, endOfDay, pageable);
                case "completed":
                    return appointmentRepository.findByStatusOrderByAppointmentTimeDesc("COMPLETED", pageable);
                case "cancelled":
                    return appointmentRepository.findByStatusOrderByAppointmentTimeDesc("CANCELLED", pageable);
                default:
                    break;
            }
        }
        
        // Apply filters
        if (search != null && !search.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                return appointmentRepository.findByClientNameContainingIgnoreCaseAndStatusOrderByAppointmentTimeDesc(
                    search, status, pageable);
            } else {
                return appointmentRepository.findByClientNameContainingIgnoreCaseOrderByAppointmentTimeDesc(
                    search, pageable);
            }
        }
        
        if (status != null && !status.isEmpty()) {
            return appointmentRepository.findByStatusOrderByAppointmentTimeDesc(status, pageable);
        }
        
        if (fromDate != null && toDate != null) {
            return appointmentRepository.findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(
                fromDate, toDate, pageable);
        }
        
        // Default: return all appointments
        return appointmentRepository.findAllByOrderByAppointmentTimeDesc(pageable);
    }
    
    /**
     * Get total count of appointments
     */
    public long getTotalAppointments() {
        return appointmentRepository.count();
    }
    
    /**
     * Get appointments by status
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatusOrderByAppointmentTimeDesc(status);
    }
    
    /**
     * Get appointments by specific date
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(startOfDay, endOfDay);
    }
    
    /**
     * Get count of pending appointments by date
     */
    public long getPendingAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.countByAppointmentTimeBetweenAndStatus(startOfDay, endOfDay, "PENDING");
    }
    
    /**
     * Get count of cancelled appointments by date
     */
    public long getCancelledAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.countByAppointmentTimeBetweenAndStatus(startOfDay, endOfDay, "CANCELLED");
    }
    
    /**
     * Get count of rescheduled appointments by date
     */
    public long getRescheduledAppointmentsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentRepository.countByRescheduledAtBetween(startOfDay, endOfDay);
    }
    
    /**
     * Get appointment by ID (admin access)
     */
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));
    }
    
    /**
     * Update appointment (admin access)
     */
    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {
        Appointment existingAppointment = getAppointmentById(id);
        
        // Update fields
        if (updatedAppointment.getAppointmentTime() != null) {
            existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        }
        if (updatedAppointment.getStatus() != null) {
            String oldStatus = existingAppointment.getStatus();
            existingAppointment.setStatus(updatedAppointment.getStatus());
            
            // Send notification if status changed
            if (!oldStatus.equals(updatedAppointment.getStatus())) {
                try {
                    notificationService.notifyAppointmentStatusChanged(existingAppointment, oldStatus, updatedAppointment.getStatus());
                } catch (Exception e) {
                    System.err.println("Failed to send status change notification: " + e.getMessage());
                }
            }
        }
        if (updatedAppointment.getNotes() != null) {
            existingAppointment.setNotes(updatedAppointment.getNotes());
        }
        if (updatedAppointment.getServiceType() != null) {
            existingAppointment.setServiceType(updatedAppointment.getServiceType());
        }
          existingAppointment.setUpdatedAt(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(existingAppointment);
        
        // Log appointment update
        try {
            auditService.logAppointmentUpdated(
                id, 
                "Appointment details updated by admin", 
                null, 
                null
            );
        } catch (Exception e) {
            System.err.println("Failed to log appointment update: " + e.getMessage());
        }
        
        return saved;
    }
    
    /**
     * Delete appointment (admin access)
     */
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
          // Send cancellation notifications before deletion
        try {
            notificationService.notifyClient(
                appointment.getClient(),
                "Appointment Cancelled",
                "Your appointment scheduled for " + appointment.getAppointmentTime() + " has been cancelled by the administrator."
            );
            notificationService.notifyProvider(
                appointment.getProvider(),
                "Appointment Cancelled",
                "The appointment with " + appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName() + 
                " scheduled for " + appointment.getAppointmentTime() + " has been cancelled by the administrator."
            );
        } catch (Exception e) {
            System.err.println("Failed to send cancellation notifications: " + e.getMessage());
        }
        
        // Log appointment deletion
        try {
            auditService.logAppointmentDeleted(
                id, 
                String.format("Appointment between %s and %s deleted by admin", 
                    appointment.getClientFullName(), appointment.getProviderFullName()),
                null, 
                null
            );
        } catch (Exception e) {
            System.err.println("Failed to log appointment deletion: " + e.getMessage());
        }
        
        appointmentRepository.deleteById(id);
    }
    
    /**
     * Create appointment by admin
     */
    public Appointment createAppointmentByAdmin(Map<String, Object> appointmentData) {
        Appointment appointment = new Appointment();
        
        // Extract and set appointment data
        Long clientId = Long.valueOf(appointmentData.get("clientId").toString());
        Long providerId = Long.valueOf(appointmentData.get("providerId").toString());
        
        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client not found"));
        User provider = userRepository.findById(providerId)
            .orElseThrow(() -> new RuntimeException("Provider not found"));
        
        appointment.setClient(client);
        appointment.setProvider(provider);
        appointment.setAppointmentTime(LocalDateTime.parse(appointmentData.get("appointmentTime").toString()));
        appointment.setServiceType(appointmentData.get("serviceType").toString());
        appointment.setStatus(appointmentData.getOrDefault("status", "PENDING").toString());
        appointment.setNotes(appointmentData.getOrDefault("notes", "").toString());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send notifications
        try {
            notificationService.notifyAppointmentBooked(savedAppointment);
        } catch (Exception e) {
            System.err.println("Failed to send booking notifications: " + e.getMessage());
        }
        
        return savedAppointment;
    }
    
    /**
     * Export appointments to CSV/Excel
     */
    public byte[] exportAppointments(String format, String fromDate, String toDate) throws IOException {
        List<Appointment> appointments;
        
        if (fromDate != null && toDate != null) {
            LocalDateTime from = LocalDate.parse(fromDate).atStartOfDay();
            LocalDateTime to = LocalDate.parse(toDate).atTime(23, 59, 59);
            appointments = appointmentRepository.findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(from, to);
        } else {
            appointments = appointmentRepository.findAllByOrderByAppointmentTimeDesc();
        }
        
        if ("csv".equalsIgnoreCase(format) || format == null) {
            return exportToCSV(appointments);
        } else {
            // For now, default to CSV. Excel export can be implemented later
            return exportToCSV(appointments);
        }
    }
    
    private byte[] exportToCSV(List<Appointment> appointments) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        
        // Write CSV header
        writer.write("ID,Client Name,Provider Name,Appointment Time,Service Type,Status,Notes,Created At\n");
        
        // Write appointment data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Appointment appointment : appointments) {
            writer.write(String.format("%d,\"%s %s\",\"%s %s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                appointment.getId(),
                appointment.getClient().getFirstName(),
                appointment.getClient().getLastName(),
                appointment.getProvider().getFirstName(),
                appointment.getProvider().getLastName(),
                appointment.getAppointmentTime().format(formatter),
                appointment.getServiceType(),
                appointment.getStatus(),
                appointment.getNotes() != null ? appointment.getNotes().replace("\"", "\"\"") : "",
                appointment.getCreatedAt().format(formatter)
            ));
        }
          writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    // Get appointments between dates
    public List<Appointment> getAllAppointmentsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByCreatedAtBetween(startDate, endDate);
    }

    // Get appointments by provider and date range
    public List<Appointment> getAppointmentsByProviderAndDateRange(Long providerId, LocalDateTime startDate, LocalDateTime endDate) {
        Optional<User> provider = userRepository.findById(providerId);
        if (provider.isPresent()) {
            return appointmentRepository.findByProviderAndCreatedAtBetween(provider.get(), startDate, endDate);
        }
        return List.of();
    }
}
