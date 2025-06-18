package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/credentials")
    public String credentials() {
        return "debug/credentials";
    }

    @GetMapping("/users")
    @ResponseBody
    public Map<String, Object> debugUsers() {
        Map<String, Object> result = new HashMap<>();
        
        // Get all users
        List<User> users = userRepository.findAll();
        result.put("totalUsers", users.size());
        
        // Check specific test users
        Map<String, Object> testUsers = new HashMap<>();
        
        User admin = userRepository.findByEmail("admin@admin.com").orElse(null);
        if (admin != null) {
            testUsers.put("admin@admin.com", Map.of(
                "exists", true,
                "role", admin.getRole(),
                "status", admin.getStatus(),
                "isActive", admin.isActive(),
                "passwordMatches", passwordEncoder.matches("password123", admin.getPassword())
            ));
        } else {
            testUsers.put("admin@admin.com", Map.of("exists", false));
        }
        
        User provider = userRepository.findByEmail("provider@provider.com").orElse(null);
        if (provider != null) {
            testUsers.put("provider@provider.com", Map.of(
                "exists", true,
                "role", provider.getRole(),
                "status", provider.getStatus(),
                "isActive", provider.isActive(),
                "passwordMatches", passwordEncoder.matches("password123", provider.getPassword())
            ));
        } else {
            testUsers.put("provider@provider.com", Map.of("exists", false));
        }
        
        User client = userRepository.findByEmail("john.doe@email.com").orElse(null);
        if (client != null) {
            testUsers.put("john.doe@email.com", Map.of(
                "exists", true,
                "role", client.getRole(),
                "status", client.getStatus(),
                "isActive", client.isActive(),
                "passwordMatches", passwordEncoder.matches("password123", client.getPassword())
            ));
        } else {
            testUsers.put("john.doe@email.com", Map.of("exists", false));
        }
        
        result.put("testUsers", testUsers);
        
        return result;
    }
}
