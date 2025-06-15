package com.smartappointments.booking_system.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String index(){
        return "admin/admin-dashboard";
    }
@GetMapping("/manage-user")
    public String manageuser(){
        return "admin/manage-user";
}
    @GetMapping("/view-appointments")
    public String viewappointment() {
        return "admin/view-appointments";
    }

    @GetMapping("/system-settings")
    public String systemsettings() {
        return "admin/system-settings";
    }

    @GetMapping("/report")
    public String repost() {
        return "admin/report";
    }

    @GetMapping("/audit-logs")
    public String audit() {
        return "admin/audit-logs";
    }
}
