package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.service.AdminService;
import com.smartappointments.booking_system.service.UserService;
import com.smartappointments.booking_system.service.AppointmentService;
import com.smartappointments.booking_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    // Dashboard remains unchanged
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // ... existing dashboard code ...
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

        boolean newStatus = adminService.updateUserStatus(userId, payload.get("active"));
        return ResponseEntity.ok(Map.of(
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
    public String auditLogs(Model model, @RequestParam(required = false) String keyword) {
        // ... existing code ...
        return "admin/audit-logs";
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
}