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

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.getAllUsers().size());
        model.addAttribute("activeAppointments", adminService.getAppointmentsByStatus("CONFIRMED"));
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