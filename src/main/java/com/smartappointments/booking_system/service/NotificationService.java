package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * Send notification when a new appointment is booked
     */
    public void notifyAppointmentBooked(Appointment appointment) {
        try {
            // Notify the provider
            notifyProvider(appointment);
            
            // Notify the client (confirmation)
            notifyClient(appointment);
            
            // Notify admin (optional - for monitoring)
            notifyAdmin(appointment);
            
            logger.info("Notifications sent for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            logger.error("Failed to send notifications for appointment ID: {}", appointment.getId(), e);
        }
    }
    
    /**
     * Send notification when appointment status changes
     */
    public void notifyAppointmentStatusChanged(Appointment appointment, String oldStatus, String newStatus) {
        try {
            // Notify the client about status change
            notifyClientStatusChange(appointment, oldStatus, newStatus);
            
            // If approved by provider, notify admin
            if ("CONFIRMED".equals(newStatus)) {
                notifyAdminAppointmentConfirmed(appointment);
            }
            
            logger.info("Status change notifications sent for appointment ID: {} ({} -> {})", 
                       appointment.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            logger.error("Failed to send status change notifications for appointment ID: {}", 
                        appointment.getId(), e);
        }
    }
    
    /**
     * Notify provider about new appointment booking
     */
    private void notifyProvider(Appointment appointment) {
        User provider = appointment.getProvider();
        String message = String.format(
            "New appointment booked:\n" +
            "Client: %s %s\n" +
            "Date: %s\n" +
            "Service: %s\n" +
            "Notes: %s",
            appointment.getClient().getFirstName(),
            appointment.getClient().getLastName(),
            appointment.getAppointmentTime().toString(),
            appointment.getServiceType(),
            appointment.getClientNotes()
        );
        
        // In a real application, this would send email/SMS
        logger.info("PROVIDER NOTIFICATION - {}: {}", provider.getEmail(), message);
        
        // TODO: Implement actual email/SMS sending
        // emailService.sendEmail(provider.getEmail(), "New Appointment Booking", message);
        // smsService.sendSMS(provider.getPhoneNumber(), message);
    }
    
    /**
     * Notify client about appointment confirmation
     */
    private void notifyClient(Appointment appointment) {
        User client = appointment.getClient();
        String message = String.format(
            "Appointment booked successfully!\n" +
            "Provider: Dr. %s %s\n" +
            "Date: %s\n" +
            "Service: %s\n" +
            "Status: %s\n\n" +
            "You will receive a confirmation once the provider approves your appointment.",
            appointment.getProvider().getFirstName(),
            appointment.getProvider().getLastName(),
            appointment.getAppointmentTime().toString(),
            appointment.getServiceType(),
            appointment.getStatus()
        );
        
        logger.info("CLIENT NOTIFICATION - {}: {}", client.getEmail(), message);
        
        // TODO: Implement actual email/SMS sending
        // emailService.sendEmail(client.getEmail(), "Appointment Confirmation", message);
    }
    
    /**
     * Notify admin about new appointment for monitoring
     */
    private void notifyAdmin(Appointment appointment) {
        String message = String.format(
            "New appointment in the system:\n" +
            "ID: %d\n" +
            "Client: %s %s (%s)\n" +
            "Provider: Dr. %s %s (%s)\n" +
            "Date: %s\n" +
            "Service: %s\n" +
            "Status: %s",
            appointment.getId(),
            appointment.getClient().getFirstName(),
            appointment.getClient().getLastName(),
            appointment.getClient().getEmail(),
            appointment.getProvider().getFirstName(),
            appointment.getProvider().getLastName(),
            appointment.getProvider().getEmail(),
            appointment.getAppointmentTime().toString(),
            appointment.getServiceType(),
            appointment.getStatus()
        );
        
        logger.info("ADMIN NOTIFICATION: {}", message);
        
        // TODO: Send to admin dashboard notification system
        // adminNotificationService.notifyNewAppointment(appointment);
    }
    
    /**
     * Notify client about appointment status change
     */
    private void notifyClientStatusChange(Appointment appointment, String oldStatus, String newStatus) {
        User client = appointment.getClient();
        String statusMessage = getStatusChangeMessage(newStatus);
        
        String message = String.format(
            "Your appointment status has been updated:\n" +
            "Provider: Dr. %s %s\n" +
            "Date: %s\n" +
            "Status: %s\n\n" +
            "%s",
            appointment.getProvider().getFirstName(),
            appointment.getProvider().getLastName(),
            appointment.getAppointmentTime().toString(),
            newStatus,
            statusMessage
        );
        
        logger.info("CLIENT STATUS CHANGE NOTIFICATION - {}: {}", client.getEmail(), message);
    }
    
    /**
     * Notify admin when appointment is confirmed by provider
     */
    private void notifyAdminAppointmentConfirmed(Appointment appointment) {
        String message = String.format(
            "Appointment confirmed by provider:\n" +
            "ID: %d\n" +
            "Client: %s %s\n" +
            "Provider: Dr. %s %s\n" +
            "Date: %s\n" +
            "Revenue: $%.2f",
            appointment.getId(),
            appointment.getClient().getFirstName(),
            appointment.getClient().getLastName(),
            appointment.getProvider().getFirstName(),
            appointment.getProvider().getLastName(),
            appointment.getAppointmentTime().toString(),
            calculateAppointmentFee(appointment)
        );
        
        logger.info("ADMIN CONFIRMATION NOTIFICATION: {}", message);
    }
    
    /**
     * Send appointment reminder notifications
     */
    public void sendAppointmentReminder(Appointment appointment, int hoursBeforeAppointment) {
        try {
            String reminderMessage = String.format(
                "Appointment Reminder:\n" +
                "You have an appointment in %d hours\n" +
                "Provider: Dr. %s %s\n" +
                "Date: %s\n" +
                "Service: %s\n" +
                "Location: [Clinic Address]\n\n" +
                "Please arrive 15 minutes early.",
                hoursBeforeAppointment,
                appointment.getProvider().getFirstName(),
                appointment.getProvider().getLastName(),
                appointment.getAppointmentTime().toString(),
                appointment.getServiceType()
            );
            
            User client = appointment.getClient();
            logger.info("REMINDER NOTIFICATION - {}: {}", client.getEmail(), reminderMessage);
            
            // TODO: Implement actual reminder sending
            // emailService.sendEmail(client.getEmail(), "Appointment Reminder", reminderMessage);
            // smsService.sendSMS(client.getPhoneNumber(), reminderMessage);
            
        } catch (Exception e) {
            logger.error("Failed to send appointment reminder for ID: {}", appointment.getId(), e);
        }
    }
    
    /**
     * Get appropriate message for status changes
     */
    private String getStatusChangeMessage(String status) {
        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return "Your appointment has been confirmed by the provider. We look forward to seeing you!";
            case "CANCELLED":
                return "Your appointment has been cancelled. Please contact us if you need to reschedule.";
            case "RESCHEDULED":
                return "Your appointment has been rescheduled. Please check the new date and time.";
            case "COMPLETED":
                return "Your appointment has been completed. Thank you for choosing our services!";
            default:
                return "Your appointment status has been updated.";
        }
    }
    
    /**
     * Calculate appointment fee (simplified)
     */
    private double calculateAppointmentFee(Appointment appointment) {
        // This would normally come from a pricing service or database
        switch (appointment.getServiceType().toLowerCase()) {
            case "consultation":
                return 85.0;
            case "check-up":
                return 120.0;
            case "treatment":
                return 150.0;
            default:
                return 100.0;
        }
    }
    
    /**
     * Send bulk notifications to multiple users
     */
    public void sendBulkNotification(String message, String title, java.util.List<User> recipients) {
        try {
            for (User recipient : recipients) {
                notifyUser(recipient, title, message);
            }
            logger.info("Bulk notification sent to {} recipients: {}", recipients.size(), title);
        } catch (Exception e) {
            logger.error("Failed to send bulk notification: {}", title, e);
        }
    }
    
    /**
     * Send maintenance notification to all users
     */
    public void sendMaintenanceNotification(java.util.List<User> users, java.time.LocalDateTime maintenanceTime) {
        try {
            String title = "Scheduled Maintenance Notification";
            String message = String.format(
                "Dear User,\n\n" +
                "We will be performing scheduled maintenance on our system on %s.\n" +
                "The system may be temporarily unavailable during this time.\n" +
                "We apologize for any inconvenience caused.\n\n" +
                "Best regards,\n" +
                "Smart Appointments Team",
                maintenanceTime.format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"))
            );
            
            sendBulkNotification(message, title, users);
            logger.info("Maintenance notification sent to {} users for maintenance at {}", users.size(), maintenanceTime);
        } catch (Exception e) {
            logger.error("Failed to send maintenance notification", e);
        }
    }
    
    /**
     * General method to send notification to any user
     */
    private void notifyUser(User user, String subject, String message) {
        try {
            logger.info("USER NOTIFICATION - {} ({}): {}", user.getEmail(), subject, message);
            // TODO: Implement actual email/SMS sending
            // emailService.sendEmail(user.getEmail(), subject, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to user: {}", user.getEmail(), e);
        }
    }
    
    /**
     * Notify provider with custom message
     */
    public void notifyProvider(User provider, String subject, String message) {
        try {
            logger.info("PROVIDER NOTIFICATION - {} ({}): {}", provider.getEmail(), subject, message);
            // TODO: Implement actual email/SMS sending
            // emailService.sendEmail(provider.getEmail(), subject, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to provider: {}", provider.getEmail(), e);
        }
    }
    
    /**
     * Notify client with custom message
     */
    public void notifyClient(User client, String subject, String message) {
        try {
            logger.info("CLIENT NOTIFICATION - {} ({}): {}", client.getEmail(), subject, message);
            // TODO: Implement actual email/SMS sending
            // emailService.sendEmail(client.getEmail(), subject, message);
        } catch (Exception e) {
            logger.error("Failed to send notification to client: {}", client.getEmail(), e);
        }
    }
}
