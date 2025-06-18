package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.TimeSlot;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.service.AppointmentService;
import com.smartappointments.booking_system.service.TimeSlotService;
import com.smartappointments.booking_system.service.UserService;
import com.smartappointments.booking_system.service.ProviderProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private UserService userService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ProviderProfileService providerProfileService;

    @GetMapping
    public String index(){
        return "client/client-panel";
    }

    @GetMapping("/client-panel")
    public String client(){
        return "client/client-panel";
    }

    @GetMapping("/available-slots")
    public String availableslot(){
        return "client/available-slots";
    }

    @GetMapping("/book-appointment")
    public String bookappointment(){
        return "client/book-appointment";
    }

    @GetMapping("/my-bookings")
    public String bookings() {
        return "client/my-bookings";
    }

    @GetMapping("/my-provider")
    public String myprovider() {
        return "client/my-provider";
    }

    @GetMapping("/appointment-history")
    public String appointmenthistory() {
        return "client/appointment-history";
    }

    // API Endpoints for Available Slots

    @GetMapping("/api/providers")
    @ResponseBody
    public ResponseEntity<?> getAvailableProviders() {
        try {            List<User> providers = userService.findByRole(User.Role.PROVIDER);
            List<Map<String, Object>> providerList = providers.stream()
                .<Map<String, Object>>map(provider -> {
                    ProviderProfile profile = providerProfileService.getProviderProfile(provider);
                    return Map.of(
                        "id", provider.getId(),
                        "name", provider.getFirstName() + " " + provider.getLastName(),
                        "email", provider.getEmail(),
                        "specialty", profile.getSpecialization() != null ? profile.getSpecialization() : "General",
                        "bio", profile.getBio() != null ? profile.getBio() : ""
                    );
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(providerList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/available-slots")
    @ResponseBody
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "7") int daysAhead) {
        try {
            LocalDate startDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            LocalDate endDate = startDate.plusDays(daysAhead);
            
            List<TimeSlot> availableSlots;
            if (providerId != null) {
                User provider = userService.findById(providerId).orElse(null);
                if (provider == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Provider not found"));
                }
                availableSlots = timeSlotService.getAvailableSlotsByProviderAndDateRange(provider, startDate, endDate);
            } else {
                availableSlots = timeSlotService.getAvailableSlotsByDateRange(startDate, endDate);
            }
            
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/slots-by-date")
    @ResponseBody
    public ResponseEntity<?> getSlotsByDate(
            @RequestParam String date,
            @RequestParam(required = false) Long providerId) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            
            List<TimeSlot> slots;
            if (providerId != null) {
                User provider = userService.findById(providerId).orElse(null);
                if (provider == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Provider not found"));
                }
                slots = timeSlotService.getAvailableSlotsByProviderAndDate(provider, targetDate);
            } else {
                slots = timeSlotService.getAvailableSlotsByDate(targetDate);
            }
            
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/book-appointment")
    @ResponseBody
    public ResponseEntity<?> bookAppointment(
            @RequestBody Map<String, Object> bookingData,
            Authentication authentication) {
        try {
            User client = userService.findByEmail(authentication.getName()).orElse(null);
            if (client == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Client not found"));
            }

            Long timeSlotId = Long.valueOf(bookingData.get("timeSlotId").toString());
            String clientNotes = (String) bookingData.get("clientNotes");
            String serviceType = (String) bookingData.get("serviceType");

            Optional<TimeSlot> timeSlotOpt = timeSlotService.findById(timeSlotId);
            if (timeSlotOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Time slot not found"));
            }

            TimeSlot timeSlot = timeSlotOpt.get();
            
            // Check if slot is still available
            if (timeSlot.getCurrentAttendees() >= timeSlot.getMaxAttendees()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Time slot is no longer available"));
            }

            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setClient(client);
            appointment.setProvider(timeSlot.getProvider());
            appointment.setAppointmentTime(LocalDateTime.of(timeSlot.getDate(), timeSlot.getStartTime()));
            appointment.setStatus("PENDING");
            appointment.setServiceType(serviceType != null ? serviceType : "Consultation");
            appointment.setDurationMinutes(timeSlot.getDuration());
            appointment.setClientNotes(clientNotes);

            // Save appointment and update slot
            Appointment savedAppointment = appointmentService.createAppointment(appointment);
            timeSlotService.incrementAttendees(timeSlotId);

            return ResponseEntity.ok(Map.of(
                "appointment", savedAppointment,
                "message", "Appointment booked successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/provider-availability")
    @ResponseBody
    public ResponseEntity<?> getProviderAvailability() {
        try {
            List<User> providers = userService.findByRole(User.Role.PROVIDER);
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            LocalDate weekEnd = today.plusDays(7);
              List<Map<String, Object>> availability = providers.stream()
                .<Map<String, Object>>map(provider -> {
                    ProviderProfile profile = providerProfileService.getProviderProfile(provider);
                    int todaySlots = timeSlotService.getAvailableSlotsByProviderAndDate(provider, today).size();
                    int tomorrowSlots = timeSlotService.getAvailableSlotsByProviderAndDate(provider, tomorrow).size();
                    int weekSlots = timeSlotService.getAvailableSlotsByProviderAndDateRange(provider, today, weekEnd).size();
                    
                    return Map.of(
                        "id", provider.getId(),
                        "name", provider.getFirstName() + " " + provider.getLastName(),
                        "specialty", profile.getSpecialization() != null ? profile.getSpecialization() : "General",
                        "todaySlots", todaySlots,
                        "tomorrowSlots", tomorrowSlots,
                        "weekSlots", weekSlots
                    );
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

