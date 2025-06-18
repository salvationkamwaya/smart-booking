package com.smartappointments.booking_system.Controller;


import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.service.ProviderProfileService;
import com.smartappointments.booking_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/provider")
public class ProviderController {

    @Autowired
    private ProviderProfileService providerProfileService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String index() {
        return "provider/provider-panel";
    }

    @GetMapping("/provider-panel")
    public String providerpannel() {
        return "provider/provider-panel";
    }

    @GetMapping("/add-slot")
    public String addslot() {
        return "provider/add-slot";
    }

    @GetMapping("/my-appointments")
    public String appointments() {
        return "provider/my-appointments";
    }

    @GetMapping("/settings")
    public String settings() {
        return "provider/settings";
    }    @GetMapping("/profile-settings")
    @Transactional(readOnly = true)
    public String profileSettings(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            ProviderProfile profile = providerProfileService.getProviderProfile(user);
            model.addAttribute("profile", profile);
        }
        return "provider/profile-settings";
    }    @GetMapping("/profile-settings/data")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProfileData(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            ProviderProfile profile = providerProfileService.getProviderProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile-settings/personal")
    @ResponseBody
    public ResponseEntity<?> updatePersonalInfo(@RequestBody Map<String, Object> personalData, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            ProviderProfile updatedProfile = providerProfileService.updatePersonalInfo(personalData, user);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile-settings/professional")
    @ResponseBody
    public ResponseEntity<?> updateProfessionalInfo(@RequestBody Map<String, Object> professionalData, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            ProviderProfile updatedProfile = providerProfileService.updateProfessionalInfo(professionalData, user);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile-settings")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody ProviderProfile profile, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            ProviderProfile updatedProfile = providerProfileService.updateProviderProfile(profile, user);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }@PostMapping("/profile-settings/upload-picture")
    @ResponseBody
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
            String filename = providerProfileService.uploadProfilePicture(file, user);
            Map<String, String> response = new HashMap<>();
            response.put("filename", filename);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }    }

    @GetMapping("/view-calendar")
    public String viewcalendar() {
        return "provider/view-calendar";
    }
}
