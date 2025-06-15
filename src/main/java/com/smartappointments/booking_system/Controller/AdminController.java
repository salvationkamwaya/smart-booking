package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.*;
import com.smartappointments.booking_system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get all data for dashboard
        List<User> allUsers = adminService.getAllUsers();
        List<Appointment> allAppointments = adminService.getAllAppointments();
        List<Appointment> confirmedAppointments = adminService.getAppointmentsByStatus("CONFIRMED");
        List<Appointment> recentAppointments = allAppointments.stream()
                .sorted((a1, a2) -> a2.getAppointmentTime().compareTo(a1.getAppointmentTime()))
                .limit(5)
                .toList();
        
        // Calculate statistics
        long totalUsers = allUsers.size();
        long activeAppointmentsCount = confirmedAppointments.size();
        long providersOnline = allUsers.stream()
                .filter(user -> user.getRole().toString().contains("PROVIDER") && user.isActive())
                .count();
        
        // Add data to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeAppointmentsCount", activeAppointmentsCount);
        model.addAttribute("providersOnline", providersOnline);
        model.addAttribute("satisfactionRate", 94); // This could be calculated from feedback data
        model.addAttribute("allUsers", allUsers.stream().limit(4).toList()); // Show first 4 users
        model.addAttribute("recentAppointments", recentAppointments);
        
        return "admin/admin-dashboard";
    }

    @GetMapping("/manage-user")
    public String manageUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/manage-user";
    }

    @PostMapping("/manage-user/toggle-status/{userId}")
    public String toggleUserStatus(@PathVariable Long userId) {
        adminService.updateUserStatus(userId, false); // Toggle logic can be added
        return "redirect:/admin/manage-user";
    }

    @GetMapping("/view-appointments")
    public String viewAppointments(Model model, @RequestParam(required = false) String status) {
        List<Appointment> appointments = (status == null)
                ? adminService.getAllAppointments()
                : adminService.getAppointmentsByStatus(status);
        model.addAttribute("appointments", appointments);
        return "admin/view-appointments";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("logs", adminService.getAuditLogs(keyword != null ? keyword : ""));
        return "admin/audit-logs";
    }
}