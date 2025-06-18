package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.service.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            String role = userPrincipal.getUser().getRole().name();
            
            System.out.println("Dashboard accessed by user: " + userPrincipal.getUser().getEmail() + ", Role: " + role);
            
            switch (role) {
                case "ADMIN":
                    System.out.println("Redirecting to admin dashboard");
                    return "redirect:/admin/dashboard";
                case "PROVIDER":
                    System.out.println("Redirecting to provider dashboard");
                    return "redirect:/provider";
                case "CLIENT":
                    System.out.println("Redirecting to client dashboard");
                    return "redirect:/client";
                default:
                    System.out.println("Unknown role: " + role + ", redirecting to login");
                    return "redirect:/auth/login";
            }
        }
        
        return "redirect:/auth/login";
    }
}
