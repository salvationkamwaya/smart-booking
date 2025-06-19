package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.service.AdminService;
import com.smartappointments.booking_system.service.UserService;
import com.smartappointments.booking_system.service.AppointmentService;
import com.smartappointments.booking_system.service.NotificationService;
import com.smartappointments.booking_system.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    // Dashboard remains unchanged
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/admin-dashboard";
    }

    // Updated to support pagination and filters
    @GetMapping("/manage-user")
    public String manageUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<User> users = adminService.searchUsers(search, role, status, PageRequest.of(page, 10));
        model.addAttribute("users", users.getContent());
        model.addAttribute("totalPages", users.getTotalPages());
        return "admin/manage-user";
    }

    // API endpoint for AJAX calls
    @ResponseBody
    @GetMapping("/manage-user/api")
    public Page<User> getUsersApi(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return adminService.searchUsers(search, role, status, pageable);
    }

    // Enhanced status toggle
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    @PostMapping("/manage-user/toggle-status/{userId}")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> payload) {

        boolean newStatus = adminService.updateUserStatus(userId, payload.get("active"));        return ResponseEntity.ok(Map.of(
                "status", "success",
                "newStatus", newStatus,
                "userId", userId
        ));
    }

    // New endpoint for adding users
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    @PostMapping("/manage-user")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody User user) {
        User savedUser = adminService.createUser(user);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "user", savedUser
        ));
    }

    // View appointments remains unchanged
    @GetMapping("/view-appointments")
    public String viewAppointments(Model model, @RequestParam(required = false) String status) {
        // ... existing code ...
        return "admin/view-appointments";
    }

    // Audit logs remains unchanged
    @GetMapping("/audit-logs")
    public String auditLogs() {
        return "admin/audit-logs";
    }

    // Reports page
    @GetMapping("/report")
    public String report() {
        return "admin/report";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) User.Status status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> users = userService.getUsers(role, status, search, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("totalPages", users.getTotalPages());
        response.put("totalElements", users.getTotalElements());

        // Add counts for different roles and statuses
        Map<String, Long> counts = new HashMap<>();
        counts.put("totalClients", userService.countByRoleAndStatus(User.Role.CLIENT, User.Status.ACTIVE));
        counts.put("totalProviders", userService.countByRoleAndStatus(User.Role.PROVIDER, User.Status.ACTIVE));
        counts.put("totalAdmins", userService.countByRoleAndStatus(User.Role.ADMIN, User.Status.ACTIVE));
        counts.put("totalPending", userService.countByRoleAndStatus(null, User.Status.PENDING));

        response.put("counts", counts);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User newUser = userService.createUser(user);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/users/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam User.Status status) {
        try {
            User updatedUser = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Recent Bookings API
    @GetMapping("/api/recent-bookings")
    @ResponseBody
    public ResponseEntity<?> getRecentBookings(@RequestParam(defaultValue = "10") int limit) {
        try {
            // Get recent appointments across all providers
            java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(7);
            java.util.List<Map<String, Object>> recentBookings = new java.util.ArrayList<>();

            // This would typically use a custom repository method
            // For now, we'll get all recent appointments
            java.util.List<Appointment> appointments = appointmentService.getAllAppointmentsSince(since);

            for (Appointment appointment : appointments) {
                Map<String, Object> bookingInfo = new HashMap<>();
                bookingInfo.put("id", appointment.getId());
                bookingInfo.put("clientName", appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName());
                bookingInfo.put("providerName", "Dr. " + appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName());
                bookingInfo.put("appointmentTime", appointment.getAppointmentTime().toString());
                bookingInfo.put("serviceType", appointment.getServiceType());
                bookingInfo.put("status", appointment.getStatus());
                bookingInfo.put("createdAt", appointment.getCreatedAt().toString());
                recentBookings.add(bookingInfo);
            }

            return ResponseEntity.ok(recentBookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Send Bulk Notification API
    @PostMapping("/api/send-notification")
    @ResponseBody
    public ResponseEntity<?> sendBulkNotification(@RequestBody Map<String, Object> notificationData) {
        try {
            String message = (String) notificationData.get("message");
            String title = (String) notificationData.get("title");
            String recipientType = (String) notificationData.get("recipientType"); // "ALL", "PROVIDERS", "CLIENTS"

            java.util.List<User> recipients = new java.util.ArrayList<>();

            switch (recipientType.toUpperCase()) {
                case "ALL":
                    recipients = userService.findAll();
                    break;
                case "PROVIDERS":
                    recipients = userService.findByRole(User.Role.PROVIDER);
                    break;
                case "CLIENTS":
                    recipients = userService.findByRole(User.Role.CLIENT);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid recipient type"));
            }

            notificationService.sendBulkNotification(message, title, recipients);

            return ResponseEntity.ok(Map.of(
                    "message", "Notifications sent successfully",
                    "recipientCount", recipients.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // System Statistics API
    @GetMapping("/api/system-stats")
    @ResponseBody
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // User statistics
            long totalUsers = userService.countAllUsers();
            long totalProviders = userService.findByRole(User.Role.PROVIDER).size();
            long totalClients = userService.findByRole(User.Role.CLIENT).size();

            stats.put("totalUsers", totalUsers);
            stats.put("totalProviders", totalProviders);
            stats.put("totalClients", totalClients);

            // Appointment statistics (last 30 days)
            java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
            java.util.List<Appointment> recentAppointments = appointmentService.getAllAppointmentsSince(thirtyDaysAgo);

            long totalAppointments = recentAppointments.size();
            long confirmedAppointments = recentAppointments.stream()
                    .filter(a -> "CONFIRMED".equals(a.getStatus()))
                    .count();
            long pendingAppointments = recentAppointments.stream()
                    .filter(a -> "PENDING".equals(a.getStatus()))
                    .count();
            long cancelledAppointments = recentAppointments.stream()
                    .filter(a -> "CANCELLED".equals(a.getStatus()))
                    .count();

            stats.put("appointmentsLast30Days", totalAppointments);
            stats.put("confirmedAppointments", confirmedAppointments);
            stats.put("pendingAppointments", pendingAppointments);
            stats.put("cancelledAppointments", cancelledAppointments);

            // Revenue estimation (simplified)
            double estimatedRevenue = recentAppointments.stream()
                    .filter(a -> "CONFIRMED".equals(a.getStatus()) || "COMPLETED".equals(a.getStatus()))
                    .mapToDouble(a -> calculateAppointmentFee(a.getServiceType()))
                    .sum();

            stats.put("estimatedRevenueLast30Days", estimatedRevenue);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Maintenance Notification API
    @PostMapping("/api/maintenance-notification")
    @ResponseBody
    public ResponseEntity<?> sendMaintenanceNotification(@RequestBody Map<String, Object> maintenanceData) {
        try {
            String maintenanceTimeStr = (String) maintenanceData.get("maintenanceTime");
            java.time.LocalDateTime maintenanceTime = java.time.LocalDateTime.parse(maintenanceTimeStr);

            java.util.List<User> allUsers = userService.findAll();
            notificationService.sendMaintenanceNotification(allUsers, maintenanceTime);

            return ResponseEntity.ok(Map.of(
                    "message", "Maintenance notification sent to all users",
                    "recipientCount", allUsers.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method to calculate appointment fees
    private double calculateAppointmentFee(String serviceType) {
        switch (serviceType.toLowerCase()) {
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
    
    // Appointment Management API Endpoints
    @GetMapping("/api/appointments")
    @ResponseBody
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            LocalDateTime fromDateTime = null;
            LocalDateTime toDateTime = null;
            
            if (fromDate != null && !fromDate.isEmpty()) {
                fromDateTime = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                toDateTime = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
            
            Page<Appointment> appointments = appointmentService.getFilteredAppointments(
                search, status, fromDateTime, toDateTime, tab, PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments.getContent());
            response.put("totalPages", appointments.getTotalPages());
            response.put("totalElements", appointments.getTotalElements());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/appointments/stats")
    @ResponseBody
    public ResponseEntity<?> getAppointmentStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get total counts
            long totalAppointments = appointmentService.getTotalAppointments();
            long completedAppointments = appointmentService.getAppointmentsByStatus("COMPLETED").size();
            long cancelledAppointments = appointmentService.getAppointmentsByStatus("CANCELLED").size();
            long pendingAppointments = appointmentService.getAppointmentsByStatus("PENDING").size();
            
            // Get today's stats
            LocalDate today = LocalDate.now();
            long todayAppointments = appointmentService.getAppointmentsByDate(today).size();
            long todayPending = appointmentService.getPendingAppointmentsByDate(today);
            long todayCancelled = appointmentService.getCancelledAppointmentsByDate(today);
            long todayRescheduled = appointmentService.getRescheduledAppointmentsByDate(today);
            
            stats.put("total", totalAppointments);
            stats.put("completed", completedAppointments);
            stats.put("cancelled", cancelledAppointments);
            stats.put("pending", pendingAppointments);
            stats.put("todayTotal", todayAppointments);
            stats.put("todayPending", todayPending);
            stats.put("todayCancelled", todayCancelled);
            stats.put("todayRescheduled", todayRescheduled);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<?> getAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointment);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/appointments")
    @ResponseBody
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> appointmentData) {
        try {
            Appointment appointment = appointmentService.createAppointmentByAdmin(appointmentData);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/appointments/export")
    public ResponseEntity<?> exportAppointments(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            // Implementation for exporting appointments as CSV/Excel
            byte[] exportData = appointmentService.exportAppointments(format, fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=appointments." + (format != null ? format : "csv"));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }    
    // AUDIT LOG MANAGEMENT ENDPOINTS
    
    @GetMapping("/api/audit-logs")
    @ResponseBody
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            LocalDateTime fromDateTime = null;
            LocalDateTime toDateTime = null;
            
            if (fromDate != null && !fromDate.isEmpty()) {
                fromDateTime = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                toDateTime = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
            
            Page<AuditLog> logs = auditService.getAuditLogs(
                search, entityType, severity, success, userId, 
                fromDateTime, toDateTime, PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs.getContent());
            response.put("totalPages", logs.getTotalPages());
            response.put("totalElements", logs.getTotalElements());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/stats")
    @ResponseBody
    public ResponseEntity<?> getAuditStats() {
        try {
            Map<String, Object> stats = auditService.getAuditStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/{id}")
    @ResponseBody
    public ResponseEntity<?> getAuditLog(@PathVariable Long id) {
        try {
            AuditLog auditLog = auditService.getAuditLogById(id);
            return ResponseEntity.ok(auditLog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/recent")
    @ResponseBody
    public ResponseEntity<?> getRecentAuditLogs() {
        try {
            List<AuditLog> recentLogs = auditService.getRecentAuditLogs();
            return ResponseEntity.ok(recentLogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/failed")
    @ResponseBody
    public ResponseEntity<?> getFailedOperations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<AuditLog> failedLogs = auditService.getFailedOperations(PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", failedLogs.getContent());
            response.put("totalPages", failedLogs.getTotalPages());
            response.put("totalElements", failedLogs.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/critical")
    @ResponseBody
    public ResponseEntity<?> getCriticalEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<AuditLog> criticalLogs = auditService.getCriticalEvents(PageRequest.of(page, size));
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", criticalLogs.getContent());
            response.put("totalPages", criticalLogs.getTotalPages());
            response.put("totalElements", criticalLogs.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/audit-logs/export")
    public ResponseEntity<?> exportAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "csv") String format) {
        try {
            LocalDateTime fromDateTime = null;
            LocalDateTime toDateTime = null;
            
            if (fromDate != null && !fromDate.isEmpty()) {
                fromDateTime = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                toDateTime = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
            
            List<AuditLog> logs = auditService.exportAuditLogs(
                search, entityType, severity, success, userId, fromDateTime, toDateTime);
            
            byte[] exportData = exportAuditLogsToCSV(logs);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=audit-logs." + format);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper method to export audit logs to CSV
    private byte[] exportAuditLogsToCSV(List<AuditLog> logs) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        
        // Write CSV header
        writer.write("ID,Timestamp,Action,Entity Type,Entity ID,User,IP Address,Severity,Success,Details\n");
        
        // Write audit log data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLog log : logs) {
            writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                log.getId(),
                log.getTimestamp().format(formatter),
                log.getAction(),
                log.getEntityType() != null ? log.getEntityType() : "",
                log.getEntityId() != null ? log.getEntityId().toString() : "",
                log.getPerformedByName(),
                log.getIpAddress() != null ? log.getIpAddress() : "",
                log.getSeverity(),
                log.getSuccess().toString(),
                log.getDetails() != null ? log.getDetails().replace("\"", "\"\"") : ""
            ));
        }
          writer.flush();
        writer.close();
        return outputStream.toByteArray();
    }

    // ========== REPORTING ENDPOINTS ==========

    /**
     * Get comprehensive business analytics report
     */
    @GetMapping("/api/reports/analytics")
    @ResponseBody
    public ResponseEntity<?> getBusinessAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            LocalDateTime endDateTime;
            LocalDateTime startDateTime;
            
            // Determine date range
            if (startDate != null && endDate != null) {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            } else {
                endDateTime = LocalDateTime.now();
                switch (period != null ? period : "30d") {
                    case "7d":
                        startDateTime = endDateTime.minusDays(7);
                        break;
                    case "30d":
                        startDateTime = endDateTime.minusDays(30);
                        break;
                    case "90d":
                        startDateTime = endDateTime.minusDays(90);
                        break;
                    case "1y":
                        startDateTime = endDateTime.minusYears(1);
                        break;
                    default:
                        startDateTime = endDateTime.minusDays(30);
                }
            }

            // User metrics
            long totalUsers = userService.countAllUsers();
            long totalClients = userService.findByRole(User.Role.CLIENT).size();
            long totalProviders = userService.findByRole(User.Role.PROVIDER).size();
            long newUsersInPeriod = userService.getUsersCreatedBetween(startDateTime, endDateTime).size();

            analytics.put("userMetrics", Map.of(
                "totalUsers", totalUsers,
                "totalClients", totalClients,
                "totalProviders", totalProviders,
                "newUsersInPeriod", newUsersInPeriod,
                "clientProviderRatio", totalProviders > 0 ? (double) totalClients / totalProviders : 0
            ));

            // Appointment metrics
            List<Appointment> allAppointments = appointmentService.getAllAppointmentsBetween(startDateTime, endDateTime);
            long totalAppointments = allAppointments.size();
            long completedAppointments = allAppointments.stream().mapToLong(a -> "COMPLETED".equals(a.getStatus()) ? 1 : 0).sum();
            long cancelledAppointments = allAppointments.stream().mapToLong(a -> "CANCELLED".equals(a.getStatus()) ? 1 : 0).sum();
            long pendingAppointments = allAppointments.stream().mapToLong(a -> "PENDING".equals(a.getStatus()) ? 1 : 0).sum();
            double completionRate = totalAppointments > 0 ? (double) completedAppointments / totalAppointments * 100 : 0;
            double cancellationRate = totalAppointments > 0 ? (double) cancelledAppointments / totalAppointments * 100 : 0;

            analytics.put("appointmentMetrics", Map.of(
                "totalAppointments", totalAppointments,
                "completedAppointments", completedAppointments,
                "cancelledAppointments", cancelledAppointments,
                "pendingAppointments", pendingAppointments,
                "completionRate", Math.round(completionRate * 100.0) / 100.0,
                "cancellationRate", Math.round(cancellationRate * 100.0) / 100.0
            ));

            // Revenue estimation (simplified)
            double estimatedRevenue = allAppointments.stream()
                    .filter(a -> "COMPLETED".equals(a.getStatus()))
                    .mapToDouble(a -> calculateAppointmentFee(a.getServiceType()))
                    .sum();

            analytics.put("revenueMetrics", Map.of(
                "estimatedRevenue", Math.round(estimatedRevenue * 100.0) / 100.0,
                "averageRevenuePerAppointment", totalAppointments > 0 ? Math.round((estimatedRevenue / totalAppointments) * 100.0) / 100.0 : 0
            ));

            // Activity metrics
            Map<String, Object> auditStats = auditService.getAuditStatistics();
            analytics.put("activityMetrics", Map.of(
                "totalLogs", auditStats.get("totalLogs"),
                "todayLogs", auditStats.get("todayLogs"),
                "errorRate", auditStats.get("todayErrors"),
                "failureRate", auditStats.get("todayFailures")
            ));

            analytics.put("period", Map.of(
                "startDate", startDateTime.toLocalDate().toString(),
                "endDate", endDateTime.toLocalDate().toString(),
                "label", period != null ? period : "30d"
            ));

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate analytics: " + e.getMessage()));
        }
    }

    /**
     * Get appointment trends data
     */
    @GetMapping("/api/reports/appointment-trends")
    @ResponseBody
    public ResponseEntity<?> getAppointmentTrends(
            @RequestParam(required = false, defaultValue = "30") int days) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            List<Map<String, Object>> trends = new ArrayList<>();
            
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i).toLocalDate();
                List<Appointment> dayAppointments = appointmentService.getAppointmentsByDate(date);
                
                long completed = dayAppointments.stream().mapToLong(a -> "COMPLETED".equals(a.getStatus()) ? 1 : 0).sum();
                long cancelled = dayAppointments.stream().mapToLong(a -> "CANCELLED".equals(a.getStatus()) ? 1 : 0).sum();
                long pending = dayAppointments.stream().mapToLong(a -> "PENDING".equals(a.getStatus()) ? 1 : 0).sum();
                
                trends.add(Map.of(
                    "date", date.toString(),
                    "total", dayAppointments.size(),
                    "completed", completed,
                    "cancelled", cancelled,
                    "pending", pending
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "trends", trends,
                "summary", Map.of(
                    "totalDays", days,
                    "averagePerDay", trends.stream().mapToInt(t -> (Integer) t.get("total")).average().orElse(0),
                    "peakDay", trends.stream().max((a, b) -> Integer.compare((Integer) a.get("total"), (Integer) b.get("total"))).orElse(Map.of())
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate trends: " + e.getMessage()));
        }
    }

    /**
     * Get user registration trends
     */
    @GetMapping("/api/reports/user-trends")
    @ResponseBody
    public ResponseEntity<?> getUserTrends(
            @RequestParam(required = false, defaultValue = "30") int days) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            List<Map<String, Object>> trends = new ArrayList<>();
            
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i).toLocalDate();
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.atTime(23, 59, 59);
                
                List<User> dayUsers = userService.getUsersCreatedBetween(dayStart, dayEnd);
                long clients = dayUsers.stream().mapToLong(u -> u.getRole() == User.Role.CLIENT ? 1 : 0).sum();
                long providers = dayUsers.stream().mapToLong(u -> u.getRole() == User.Role.PROVIDER ? 1 : 0).sum();
                
                trends.add(Map.of(
                    "date", date.toString(),
                    "total", dayUsers.size(),
                    "clients", clients,
                    "providers", providers
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "trends", trends,
                "summary", Map.of(
                    "totalDays", days,
                    "averagePerDay", trends.stream().mapToInt(t -> (Integer) t.get("total")).average().orElse(0),
                    "totalNewUsers", trends.stream().mapToInt(t -> (Integer) t.get("total")).sum()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate user trends: " + e.getMessage()));
        }
    }

    /**
     * Get provider performance report
     */
    @GetMapping("/api/reports/provider-performance")
    @ResponseBody
    public ResponseEntity<?> getProviderPerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime startDateTime = startDate != null ? 
                LocalDate.parse(startDate).atStartOfDay() : 
                LocalDateTime.now().minusDays(30);
            LocalDateTime endDateTime = endDate != null ? 
                LocalDate.parse(endDate).atTime(23, 59, 59) : 
                LocalDateTime.now();

            List<User> providers = userService.findByRole(User.Role.PROVIDER);
            List<Map<String, Object>> performance = new ArrayList<>();
            
            for (User provider : providers) {
                List<Appointment> providerAppointments = appointmentService.getAppointmentsByProviderAndDateRange(
                    provider.getId(), startDateTime, endDateTime);
                
                long totalAppointments = providerAppointments.size();
                long completedAppointments = providerAppointments.stream()
                    .mapToLong(a -> "COMPLETED".equals(a.getStatus()) ? 1 : 0).sum();
                long cancelledAppointments = providerAppointments.stream()
                    .mapToLong(a -> "CANCELLED".equals(a.getStatus()) ? 1 : 0).sum();
                
                double completionRate = totalAppointments > 0 ? 
                    (double) completedAppointments / totalAppointments * 100 : 0;
                
                double estimatedRevenue = providerAppointments.stream()
                    .filter(a -> "COMPLETED".equals(a.getStatus()))
                    .mapToDouble(a -> calculateAppointmentFee(a.getServiceType()))
                    .sum();
                
                performance.add(Map.of(
                    "providerId", provider.getId(),
                    "providerName", provider.getFirstName() + " " + provider.getLastName(),
                    "providerEmail", provider.getEmail(),
                    "totalAppointments", totalAppointments,
                    "completedAppointments", completedAppointments,
                    "cancelledAppointments", cancelledAppointments,
                    "completionRate", Math.round(completionRate * 100.0) / 100.0,
                    "estimatedRevenue", Math.round(estimatedRevenue * 100.0) / 100.0
                ));
            }
            
            // Sort by completion rate descending
            performance.sort((a, b) -> Double.compare(
                (Double) b.get("completionRate"), 
                (Double) a.get("completionRate")
            ));
            
            return ResponseEntity.ok(Map.of(
                "providers", performance,
                "summary", Map.of(
                    "totalProviders", providers.size(),
                    "averageCompletionRate", performance.stream()
                        .mapToDouble(p -> (Double) p.get("completionRate"))
                        .average().orElse(0),
                    "topPerformer", performance.isEmpty() ? null : performance.get(0)
                ),
                "period", Map.of(
                    "startDate", startDateTime.toLocalDate().toString(),
                    "endDate", endDateTime.toLocalDate().toString()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate provider performance: " + e.getMessage()));
        }
    }    /**
     * Export comprehensive report as CSV
     */
    @GetMapping("/api/reports/export")
    @ResponseBody
    public ResponseEntity<?> exportReport(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "csv") String format) {
        try {
            LocalDateTime startDateTime = startDate != null ? 
                LocalDate.parse(startDate).atStartOfDay() : 
                LocalDateTime.now().minusDays(30);
            LocalDateTime endDateTime = endDate != null ? 
                LocalDate.parse(endDate).atTime(23, 59, 59) : 
                LocalDateTime.now();

            StringBuilder csvContent = new StringBuilder();
            String filename;

            switch (type.toLowerCase()) {
                case "analytics":
                    csvContent = exportAnalyticsReport(startDateTime, endDateTime);
                    filename = "analytics-report";
                    break;
                case "appointments":
                    csvContent = exportAppointmentsReport(startDateTime, endDateTime);
                    filename = "appointments-report";
                    break;
                case "users":
                    csvContent = exportUsersReport(startDateTime, endDateTime);
                    filename = "users-report";
                    break;
                case "providers":
                    csvContent = exportProvidersReport(startDateTime, endDateTime);
                    filename = "providers-report";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid report type: " + type);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + filename + "-" + 
                LocalDate.now().toString() + ".csv");
            headers.add("Content-Type", "text/csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to export report: " + e.getMessage()));
        }
    }

    // Helper methods for report exports
    private StringBuilder exportAnalyticsReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder csv = new StringBuilder();
        csv.append("Smart Appointments - Analytics Report\n");
        csv.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        csv.append("Period: ").append(startDate.toLocalDate()).append(" to ").append(endDate.toLocalDate()).append("\n\n");

        // User metrics
        csv.append("USER METRICS\n");
        csv.append("Total Users,").append(userService.countAllUsers()).append("\n");
        csv.append("Total Clients,").append(userService.findByRole(User.Role.CLIENT).size()).append("\n");
        csv.append("Total Providers,").append(userService.findByRole(User.Role.PROVIDER).size()).append("\n\n");

        // Appointment metrics
        List<Appointment> appointments = appointmentService.getAllAppointmentsBetween(startDate, endDate);
        csv.append("APPOINTMENT METRICS\n");
        csv.append("Total Appointments,").append(appointments.size()).append("\n");
        csv.append("Completed,").append(appointments.stream().mapToLong(a -> "COMPLETED".equals(a.getStatus()) ? 1 : 0).sum()).append("\n");
        csv.append("Cancelled,").append(appointments.stream().mapToLong(a -> "CANCELLED".equals(a.getStatus()) ? 1 : 0).sum()).append("\n");
        csv.append("Pending,").append(appointments.stream().mapToLong(a -> "PENDING".equals(a.getStatus()) ? 1 : 0).sum()).append("\n");

        return csv;
    }

    private StringBuilder exportAppointmentsReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Client,Provider,Date,Time,Service,Status,Created Date\n");
        
        List<Appointment> appointments = appointmentService.getAllAppointmentsBetween(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (Appointment appointment : appointments) {
            csv.append(appointment.getId()).append(",");
            csv.append(appointment.getClient() != null ? 
                appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName() : "N/A").append(",");
            csv.append(appointment.getProvider() != null ? 
                appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName() : "N/A").append(",");
            csv.append(appointment.getAppointmentTime() != null ? 
                appointment.getAppointmentTime().toLocalDate().toString() : "N/A").append(",");
            csv.append(appointment.getAppointmentTime() != null ? 
                appointment.getAppointmentTime().toLocalTime().toString() : "N/A").append(",");
            csv.append(appointment.getServiceType() != null ? appointment.getServiceType() : "N/A").append(",");
            csv.append(appointment.getStatus() != null ? appointment.getStatus() : "N/A").append(",");
            csv.append(appointment.getCreatedAt() != null ? appointment.getCreatedAt().format(formatter) : "N/A").append("\n");
        }
        
        return csv;
    }

    private StringBuilder exportUsersReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Email,First Name,Last Name,Role,Active,Created Date\n");
        
        List<User> users = userService.getUsersCreatedBetween(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (User user : users) {
            csv.append(user.getId()).append(",");
            csv.append(user.getEmail()).append(",");
            csv.append(user.getFirstName()).append(",");
            csv.append(user.getLastName()).append(",");
            csv.append(user.getRole().toString()).append(",");
            csv.append(user.isActive()).append(",");
            csv.append(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "N/A").append("\n");
        }
        
        return csv;
    }

    private StringBuilder exportProvidersReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder csv = new StringBuilder();
        csv.append("Provider ID,Name,Email,Total Appointments,Completed,Cancelled,Completion Rate %,Estimated Revenue\n");
        
        List<User> providers = userService.findByRole(User.Role.PROVIDER);
        
        for (User provider : providers) {
            List<Appointment> providerAppointments = appointmentService.getAppointmentsByProviderAndDateRange(
                provider.getId(), startDate, endDate);
            
            long total = providerAppointments.size();
            long completed = providerAppointments.stream().mapToLong(a -> "COMPLETED".equals(a.getStatus()) ? 1 : 0).sum();
            long cancelled = providerAppointments.stream().mapToLong(a -> "CANCELLED".equals(a.getStatus()) ? 1 : 0).sum();
            double completionRate = total > 0 ? (double) completed / total * 100 : 0;
            double revenue = providerAppointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .mapToDouble(a -> calculateAppointmentFee(a.getServiceType()))
                .sum();
            
            csv.append(provider.getId()).append(",");
            csv.append(provider.getFirstName()).append(" ").append(provider.getLastName()).append(",");
            csv.append(provider.getEmail()).append(",");
            csv.append(total).append(",");
            csv.append(completed).append(",");
            csv.append(cancelled).append(",");
            csv.append(String.format("%.2f", completionRate)).append(",");
            csv.append(String.format("%.2f", revenue)).append("\n");
        }
        
        return csv;
    }
}