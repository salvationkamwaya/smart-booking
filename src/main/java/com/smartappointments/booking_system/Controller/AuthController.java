package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.service.AuthService;
import com.smartappointments.booking_system.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String register(){
        return "auth/register";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model){
        if (error != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @PostMapping("/perform_register")
    public String performRegister(@RequestParam("email") String email,
                                @RequestParam("firstName") String firstName,
                                @RequestParam("lastName") String lastName,
                                @RequestParam("password") String password,
                                @RequestParam("role") String role,
                                RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(email, firstName, lastName, password, role);
            redirectAttributes.addFlashAttribute("message", "Registration successful! You can now login with your email.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());            return "redirect:/auth/register";
        }
    }

    // API endpoints for role checking
    @GetMapping("/api/current-user")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", userPrincipal.getUser().getEmail());
            userInfo.put("role", userPrincipal.getUser().getRole().name());
            userInfo.put("firstName", userPrincipal.getUser().getFirstName());
            userInfo.put("lastName", userPrincipal.getUser().getLastName());
            userInfo.put("active", userPrincipal.getUser().isActive());
            
            return ResponseEntity.ok(userInfo);
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }

    @GetMapping("/api/role-check")
    @ResponseBody
    public ResponseEntity<?> checkRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication != null && authentication.isAuthenticated());
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            response.put("role", userPrincipal.getUser().getRole().name());
            response.put("authorities", authentication.getAuthorities());
        }
        
        return ResponseEntity.ok(response);
    }
}
