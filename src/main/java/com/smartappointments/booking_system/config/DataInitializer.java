package com.smartappointments.booking_system.config;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.model.TimeSlot;
import com.smartappointments.booking_system.repository.UserRepository;
import com.smartappointments.booking_system.repository.ProviderProfileRepository;
import com.smartappointments.booking_system.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                 ProviderProfileRepository providerProfileRepository,
                                 TimeSlotRepository timeSlotRepository) {
        return args -> {
            // Only initialize if database is empty
            if (userRepository.count() == 0) {
                System.out.println("Initializing sample data...");

                // Default password for all sample users
                String defaultPassword = passwordEncoder.encode("password123");

                // Create Admin Users
                User admin1 = new User();
                admin1.setEmail("admin@admin.com");
                admin1.setFirstName("Admin");
                admin1.setLastName("User");
                admin1.setRole(User.Role.ADMIN);
                admin1.setPassword(defaultPassword);
                admin1.setStatus(User.Status.ACTIVE);

                User admin2 = new User();
                admin2.setEmail("super@super.com");
                admin2.setFirstName("Super");
                admin2.setLastName("Administrator");
                admin2.setRole(User.Role.ADMIN);
                admin2.setPassword(defaultPassword);
                admin2.setStatus(User.Status.ACTIVE);

                // Create Provider Users
                User provider1 = new User();
                provider1.setEmail("provider@provider.com");
                provider1.setFirstName("Dr. Sarah");
                provider1.setLastName("Johnson");
                provider1.setRole(User.Role.PROVIDER);
                provider1.setPassword(defaultPassword);
                provider1.setStatus(User.Status.ACTIVE);

                User provider2 = new User();
                provider2.setEmail("provider1@provider.com");
                provider2.setFirstName("Dr. Michael");
                provider2.setLastName("Smith");
                provider2.setRole(User.Role.PROVIDER);
                provider2.setPassword(defaultPassword);
                provider2.setStatus(User.Status.ACTIVE);

                User provider3 = new User();
                provider3.setEmail("provider2@provider.com");
                provider3.setFirstName("Dr. Emily");
                provider3.setLastName("Brown");
                provider3.setRole(User.Role.PROVIDER);
                provider3.setPassword(defaultPassword);
                provider3.setStatus(User.Status.ACTIVE);

                User provider4 = new User();
                provider4.setEmail("dr.wilson@healthcare.com");
                provider4.setFirstName("Dr. Robert");
                provider4.setLastName("Wilson");
                provider4.setRole(User.Role.PROVIDER);
                provider4.setPassword(defaultPassword);
                provider4.setStatus(User.Status.ACTIVE); // Changed to ACTIVE for testing

                // Create Client Users
                User client1 = new User();
                client1.setEmail("client@client.com");
                client1.setFirstName("John");
                client1.setLastName("Doe");
                client1.setRole(User.Role.CLIENT);
                client1.setPassword(defaultPassword);
                client1.setStatus(User.Status.ACTIVE);

                User client2 = new User();
                client2.setEmail("jane.smith@email.com");
                client2.setFirstName("Jane");
                client2.setLastName("Smith");
                client2.setRole(User.Role.CLIENT);
                client2.setPassword(defaultPassword);
                client2.setStatus(User.Status.ACTIVE);

                User client3 = new User();
                client3.setEmail("alice.johnson@email.com");
                client3.setFirstName("Alice");
                client3.setLastName("Johnson");
                client3.setRole(User.Role.CLIENT);
                client3.setPassword(defaultPassword);
                client3.setStatus(User.Status.ACTIVE);

                User client4 = new User();
                client4.setEmail("bob.williams@email.com");
                client4.setFirstName("Bob");
                client4.setLastName("Williams");
                client4.setRole(User.Role.CLIENT);
                client4.setPassword(defaultPassword);
                client4.setStatus(User.Status.ACTIVE);

                // Save all users first
                List<User> users = Arrays.asList(
                    admin1, admin2, provider1, provider2, provider3, provider4,
                    client1, client2, client3, client4
                );

                userRepository.saveAll(users);

                // Create Provider Profiles
                ProviderProfile profile1 = new ProviderProfile();
                profile1.setUser(provider1);
                profile1.setFirstName(provider1.getFirstName());
                profile1.setLastName(provider1.getLastName());
                profile1.setBio("Experienced general practitioner with 10+ years in family medicine.");
                profile1.setProfession("General Practitioner");
                profile1.setSpecialization("Family Medicine");
                profile1.setServices(Set.of("General Checkup", "Consultation", "Preventive Care"));
                profile1.setWorkplace("City Medical Center");
                profile1.setYearsOfExperience(10);

                ProviderProfile profile2 = new ProviderProfile();
                profile2.setUser(provider2);
                profile2.setFirstName(provider2.getFirstName());
                profile2.setLastName(provider2.getLastName());
                profile2.setBio("Specialized cardiologist focusing on heart health and cardiovascular diseases.");
                profile2.setProfession("Cardiologist");
                profile2.setSpecialization("Cardiology");
                profile2.setServices(Set.of("Heart Checkup", "ECG", "Blood Pressure Monitoring"));
                profile2.setWorkplace("Heart Specialty Clinic");
                profile2.setYearsOfExperience(15);

                ProviderProfile profile3 = new ProviderProfile();
                profile3.setUser(provider3);
                profile3.setFirstName(provider3.getFirstName());
                profile3.setLastName(provider3.getLastName());
                profile3.setBio("Dentist specializing in general dentistry and oral health maintenance.");
                profile3.setProfession("Dentist");
                profile3.setSpecialization("General Dentistry");
                profile3.setServices(Set.of("Dental Cleaning", "Cavity Filling", "Oral Checkup"));
                profile3.setWorkplace("Smile Dental Clinic");
                profile3.setYearsOfExperience(8);

                ProviderProfile profile4 = new ProviderProfile();
                profile4.setUser(provider4);
                profile4.setFirstName(provider4.getFirstName());
                profile4.setLastName(provider4.getLastName());
                profile4.setBio("Physical therapist helping patients recover from injuries and improve mobility.");
                profile4.setProfession("Physical Therapist");
                profile4.setSpecialization("Rehabilitation");
                profile4.setServices(Set.of("Physical Therapy", "Sports Injury Recovery", "Pain Management"));
                profile4.setWorkplace("Recovery Plus Clinic");
                profile4.setYearsOfExperience(12);

                providerProfileRepository.saveAll(Arrays.asList(profile1, profile2, profile3, profile4));

                // Create Sample Time Slots for next few days
                LocalDate today = LocalDate.now();
                LocalDate tomorrow = today.plusDays(1);
                LocalDate dayAfter = today.plusDays(2);

                // Create slots for Provider 1 (Dr. Sarah Johnson)
                createTimeSlots(timeSlotRepository, provider1, today, 
                    Arrays.asList("09:00", "09:30", "10:00", "10:30", "14:00", "14:30", "15:00"));
                createTimeSlots(timeSlotRepository, provider1, tomorrow, 
                    Arrays.asList("08:30", "09:00", "09:30", "10:00", "11:00", "14:00", "15:30", "16:00"));
                createTimeSlots(timeSlotRepository, provider1, dayAfter, 
                    Arrays.asList("09:00", "10:00", "11:00", "14:00", "15:00", "16:00"));

                // Create slots for Provider 2 (Dr. Michael Smith)
                createTimeSlots(timeSlotRepository, provider2, today, 
                    Arrays.asList("08:00", "08:30", "11:00", "11:30", "13:30", "14:00"));
                createTimeSlots(timeSlotRepository, provider2, tomorrow, 
                    Arrays.asList("08:00", "09:00", "10:00", "13:00", "14:00", "15:00", "16:00"));
                createTimeSlots(timeSlotRepository, provider2, dayAfter, 
                    Arrays.asList("08:30", "09:30", "10:30", "13:30", "14:30", "15:30"));

                // Create slots for Provider 3 (Dr. Emily Brown)
                createTimeSlots(timeSlotRepository, provider3, today, 
                    Arrays.asList("10:00", "10:30", "11:00", "15:00", "15:30"));
                createTimeSlots(timeSlotRepository, provider3, tomorrow, 
                    Arrays.asList("09:00", "09:30", "10:00", "11:00", "14:30", "15:00", "15:30", "16:00"));

                // Create slots for Provider 4 (Dr. Robert Wilson)
                createTimeSlots(timeSlotRepository, provider4, tomorrow, 
                    Arrays.asList("08:00", "09:00", "10:00", "11:00"));
                createTimeSlots(timeSlotRepository, provider4, dayAfter, 
                    Arrays.asList("08:00", "08:30", "09:00", "09:30", "10:00", "14:00", "15:00"));

                System.out.println("Sample data initialized successfully!");
                System.out.println("Default login credentials:");
                System.out.println("Admin: admin@admin.com / password123");
                System.out.println("Provider: provider@provider.com / password123");
                System.out.println("Client: client@client.com / password123");
                System.out.println("Created time slots for " + timeSlotRepository.count() + " appointments");
            }
        };
    }

    private void createTimeSlots(TimeSlotRepository timeSlotRepository, User provider, LocalDate date, List<String> times) {
        for (String timeStr : times) {
            TimeSlot slot = new TimeSlot();
            slot.setProvider(provider);
            slot.setDate(date);
            slot.setStartTime(LocalTime.parse(timeStr));
            slot.setEndTime(LocalTime.parse(timeStr).plusMinutes(30)); // 30-minute slots
            slot.setDuration(30);
            slot.setMaxAttendees(1);
            slot.setCurrentAttendees(0);
            slot.setIsAvailable(true);
            slot.setStatus("AVAILABLE");
            slot.setNotes("Available for booking");
            
            timeSlotRepository.save(slot);
        }
    }
}
