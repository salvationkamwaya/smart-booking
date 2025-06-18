package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.service.AdminService;
import com.smartappointments.booking_system.service.UserService;
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
}