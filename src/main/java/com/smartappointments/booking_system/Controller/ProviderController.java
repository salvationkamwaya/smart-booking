package com.smartappointments.booking_system.Controller;


import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.model.TimeSlot;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.service.AppointmentService;
import com.smartappointments.booking_system.service.ProviderProfileService;
import com.smartappointments.booking_system.service.TimeSlotService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/provider")
public class ProviderController {

    @Autowired
    private ProviderProfileService providerProfileService;

    @Autowired
    private UserService userService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private AppointmentService appointmentService;

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
    
    // TimeSlot Management Endpoints
    @PostMapping("/slots/single")
    @ResponseBody
    public ResponseEntity<?> createSingleSlot(@RequestBody Map<String, Object> slotData, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            LocalDate date = LocalDate.parse((String) slotData.get("date"));
            LocalTime startTime = LocalTime.parse((String) slotData.get("startTime"));
            Integer duration = (Integer) slotData.get("duration");
            Integer maxAttendees = (Integer) slotData.get("maxAttendees");
            String notes = (String) slotData.get("notes");

            TimeSlot timeSlot = timeSlotService.createSingleSlot(user, date, startTime, duration, maxAttendees, notes);
            return ResponseEntity.ok(timeSlot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/slots/recurring")
    @ResponseBody
    public ResponseEntity<?> createRecurringSlots(@RequestBody Map<String, Object> slotData, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            LocalDate startDate = LocalDate.parse((String) slotData.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) slotData.get("endDate"));
            LocalTime startTime = LocalTime.parse((String) slotData.get("startTime"));
            LocalTime endTime = LocalTime.parse((String) slotData.get("endTime"));
            @SuppressWarnings("unchecked")
            List<String> daysList = (List<String>) slotData.get("daysOfWeek");
            Set<String> daysOfWeek = timeSlotService.convertDaysToSet(daysList.toArray(new String[0]));
            Integer maxAttendees = (Integer) slotData.get("maxAttendees");
            String notes = (String) slotData.get("notes");

            List<TimeSlot> timeSlots = timeSlotService.createRecurringSlots(user, startDate, endDate, startTime, endTime, daysOfWeek, maxAttendees, notes);
            return ResponseEntity.ok(Map.of("slots", timeSlots, "count", timeSlots.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/slots")
    @ResponseBody
    public ResponseEntity<?> getProviderSlots(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            List<TimeSlot> slots = timeSlotService.getProviderSlots(user);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/slots/{slotId}")
    @ResponseBody
    public ResponseEntity<?> deleteTimeSlot(@PathVariable Long slotId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            timeSlotService.deleteTimeSlot(slotId, user);
            return ResponseEntity.ok(Map.of("message", "Time slot deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/slots/{slotId}")
    @ResponseBody
    public ResponseEntity<?> updateTimeSlot(@PathVariable Long slotId, @RequestBody Map<String, Object> updates, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            TimeSlot timeSlot = timeSlotService.updateTimeSlot(slotId, user, updates);
            return ResponseEntity.ok(timeSlot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Appointment Management Endpoints
    
    @GetMapping("/appointments/data")
    @ResponseBody
    public ResponseEntity<?> getProviderAppointments(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            List<Appointment> appointments;
            
            if (search != null && !search.trim().isEmpty()) {
                appointments = appointmentService.searchProviderAppointments(user, search);
            } else if ("all".equals(status)) {
                appointments = appointmentService.getProviderAppointments(user);
            } else {
                appointments = appointmentService.getProviderAppointmentsByStatus(user, status.toUpperCase());
            }

            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments/stats")
    @ResponseBody
    public ResponseEntity<?> getAppointmentStats(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Map<String, Long> stats = appointmentService.getAppointmentStats(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/status")
    @ResponseBody
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> statusData,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            String newStatus = statusData.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }

            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(
                appointmentId, newStatus.toUpperCase(), user);
            
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/confirm")
    @ResponseBody
    public ResponseEntity<?> confirmAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Appointment appointment = appointmentService.confirmAppointment(appointmentId, user);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Appointment appointment = appointmentService.cancelAppointment(appointmentId, user);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Appointment appointment = appointmentService.completeAppointment(appointmentId, user);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/appointments/{appointmentId}/reschedule")
    @ResponseBody
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> rescheduleData,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            String newDateTimeStr = rescheduleData.get("appointmentTime");
            if (newDateTimeStr == null || newDateTimeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New appointment time is required"));
            }

            LocalDateTime newDateTime = LocalDateTime.parse(newDateTimeStr);
            Appointment appointment = appointmentService.rescheduleAppointment(appointmentId, newDateTime, user);
            
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }    @PutMapping("/appointments/{appointmentId}/notes")
    @ResponseBody
    public ResponseEntity<?> updateAppointmentNotes(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> notesData,
            Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            String notes = notesData.get("notes");
            Appointment appointment = appointmentService.updateAppointmentNotes(appointmentId, notes, user);
            
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
