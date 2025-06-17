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
            
            switch (role) {
                case "ROLE_ADMIN":
                    return "redirect:/admin/dashboard";
                case "ROLE_PROVIDER":
                    return "redirect:/provider";
                case "ROLE_CLIENT":
                    return "redirect:/client";
                default:
                    return "redirect:/auth/login";
            }
        }
        
        return "redirect:/auth/login";
    }
}
