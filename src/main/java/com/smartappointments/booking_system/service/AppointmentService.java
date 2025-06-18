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
        return appointmentRepository.findById(id);
    }

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

        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
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
    }

    // Create new appointment
    public Appointment createAppointment(Appointment appointment) {
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    // Search appointments
    public List<Appointment> searchProviderAppointments(User provider, String searchTerm) {
        return appointmentRepository.findByProviderAndClientNameContainingIgnoreCaseOrderByAppointmentTimeAsc(
            provider, searchTerm);
    }
}
