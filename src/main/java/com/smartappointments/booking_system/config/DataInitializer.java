package com.smartappointments.booking_system.config;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.UserRole;
import com.smartappointments.booking_system.model.Appointment;
import com.smartappointments.booking_system.model.AuditLog;
import com.smartappointments.booking_system.repository.UserRepository;
import com.smartappointments.booking_system.repository.AppointmentRepository;
import com.smartappointments.booking_system.repository.AuditLogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   AppointmentRepository appointmentRepository,
                                   AuditLogRepository auditLogRepository) {
        return args -> {
            // Only initialize if database is empty
            if (userRepository.count() == 0) {
                System.out.println("Initializing sample data...");

                // Create Admin Users
                User admin1 = new User();
                admin1.setEmail("admin@smartappointments.com");
                admin1.setFirstName("Admin");
                admin1.setLastName("User");
                admin1.setRole(UserRole.ROLE_ADMIN);
                admin1.setActive(true);

                User admin2 = new User();
                admin2.setEmail("super.admin@smartappointments.com");
                admin2.setFirstName("Super");
                admin2.setLastName("Administrator");
                admin2.setRole(UserRole.ROLE_ADMIN);
                admin2.setActive(true);

                // Create Provider Users
                User provider1 = new User();
                provider1.setEmail("dr.johnson@hospital.com");
                provider1.setFirstName("Dr. Sarah");
                provider1.setLastName("Johnson");
                provider1.setRole(UserRole.ROLE_PROVIDER);
                provider1.setActive(true);

                User provider2 = new User();
                provider2.setEmail("dr.smith@clinic.com");
                provider2.setFirstName("Dr. Michael");
                provider2.setLastName("Smith");
                provider2.setRole(UserRole.ROLE_PROVIDER);
                provider2.setActive(true);

                User provider3 = new User();
                provider3.setEmail("dr.brown@medical.com");
                provider3.setFirstName("Dr. Emily");
                provider3.setLastName("Brown");
                provider3.setRole(UserRole.ROLE_PROVIDER);
                provider3.setActive(true);

                User provider4 = new User();
                provider4.setEmail("dr.wilson@healthcare.com");
                provider4.setFirstName("Dr. Robert");
                provider4.setLastName("Wilson");
                provider4.setRole(UserRole.ROLE_PROVIDER);
                provider4.setActive(false); // Inactive provider

                // Create Client Users
                User client1 = new User();
                client1.setEmail("john.doe@email.com");
                client1.setFirstName("John");
                client1.setLastName("Doe");
                client1.setRole(UserRole.ROLE_CLIENT);
                client1.setActive(true);

                User client2 = new User();
                client2.setEmail("jane.smith@email.com");
                client2.setFirstName("Jane");
                client2.setLastName("Smith");
                client2.setRole(UserRole.ROLE_CLIENT);
                client2.setActive(true);

                User client3 = new User();
                client3.setEmail("alice.johnson@email.com");
                client3.setFirstName("Alice");
                client3.setLastName("Johnson");
                client3.setRole(UserRole.ROLE_CLIENT);
                client3.setActive(true);

                User client4 = new User();
                client4.setEmail("bob.williams@email.com");
                client4.setFirstName("Bob");
                client4.setLastName("Williams");
                client4.setRole(UserRole.ROLE_CLIENT);
                client4.setActive(true);

                User client5 = new User();
                client5.setEmail("charlie.davis@email.com");
                client5.setFirstName("Charlie");
                client5.setLastName("Davis");
                client5.setRole(UserRole.ROLE_CLIENT);
                client5.setActive(true);

                User client6 = new User();
                client6.setEmail("diana.miller@email.com");
                client6.setFirstName("Diana");
                client6.setLastName("Miller");
                client6.setRole(UserRole.ROLE_CLIENT);
                client6.setActive(false); // Inactive client

                User client7 = new User();
                client7.setEmail("edward.garcia@email.com");
                client7.setFirstName("Edward");
                client7.setLastName("Garcia");
                client7.setRole(UserRole.ROLE_CLIENT);
                client7.setActive(true);

                User client8 = new User();
                client8.setEmail("fiona.rodriguez@email.com");
                client8.setFirstName("Fiona");
                client8.setLastName("Rodriguez");
                client8.setRole(UserRole.ROLE_CLIENT);
                client8.setActive(true);

                // Save all users first
                List<User> users = Arrays.asList(
                    admin1, admin2, provider1, provider2, provider3, provider4,
                    client1, client2, client3, client4, client5, client6, client7, client8
                );
                userRepository.saveAll(users);
                System.out.println("Created " + users.size() + " users");

                // Create sample appointments
                LocalDateTime now = LocalDateTime.now();
                
                // Recent completed appointments
                Appointment apt1 = new Appointment();
                apt1.setClient(client1);
                apt1.setProvider(provider1);
                apt1.setAppointmentTime(now.minusDays(5).withHour(10).withMinute(0));
                apt1.setStatus("COMPLETED");

                Appointment apt2 = new Appointment();
                apt2.setClient(client2);
                apt2.setProvider(provider2);
                apt2.setAppointmentTime(now.minusDays(3).withHour(14).withMinute(30));
                apt2.setStatus("COMPLETED");

                Appointment apt3 = new Appointment();
                apt3.setClient(client3);
                apt3.setProvider(provider1);
                apt3.setAppointmentTime(now.minusDays(2).withHour(9).withMinute(0));
                apt3.setStatus("COMPLETED");

                // Today's appointments
                Appointment apt4 = new Appointment();
                apt4.setClient(client4);
                apt4.setProvider(provider2);
                apt4.setAppointmentTime(now.withHour(11).withMinute(0));
                apt4.setStatus("CONFIRMED");

                Appointment apt5 = new Appointment();
                apt5.setClient(client5);
                apt5.setProvider(provider3);
                apt5.setAppointmentTime(now.withHour(15).withMinute(30));
                apt5.setStatus("CONFIRMED");

                // Future appointments
                Appointment apt6 = new Appointment();
                apt6.setClient(client6);
                apt6.setProvider(provider1);
                apt6.setAppointmentTime(now.plusDays(1).withHour(10).withMinute(0));
                apt6.setStatus("CONFIRMED");

                Appointment apt7 = new Appointment();
                apt7.setClient(client7);
                apt7.setProvider(provider2);
                apt7.setAppointmentTime(now.plusDays(2).withHour(16).withMinute(0));
                apt7.setStatus("PENDING");

                Appointment apt8 = new Appointment();
                apt8.setClient(client8);
                apt8.setProvider(provider3);
                apt8.setAppointmentTime(now.plusDays(3).withHour(13).withMinute(30));
                apt8.setStatus("PENDING");

                // Cancelled appointments
                Appointment apt9 = new Appointment();
                apt9.setClient(client1);
                apt9.setProvider(provider3);
                apt9.setAppointmentTime(now.minusDays(1).withHour(12).withMinute(0));
                apt9.setStatus("CANCELLED");

                Appointment apt10 = new Appointment();
                apt10.setClient(client4);
                apt10.setProvider(provider1);
                apt10.setAppointmentTime(now.plusDays(4).withHour(14).withMinute(0));
                apt10.setStatus("CONFIRMED");

                List<Appointment> appointments = Arrays.asList(
                    apt1, apt2, apt3, apt4, apt5, apt6, apt7, apt8, apt9, apt10
                );
                appointmentRepository.saveAll(appointments);
                System.out.println("Created " + appointments.size() + " appointments");

                // Create audit logs
                AuditLog log1 = new AuditLog();
                log1.setAction("SYSTEM_INITIALIZED");
                log1.setDetails("Sample data initialized successfully");
                log1.setTimestamp(now);
                log1.setPerformedBy(admin1);

                AuditLog log2 = new AuditLog();
                log2.setAction("USER_CREATED");
                log2.setDetails("New client user registered: " + client1.getEmail());
                log2.setTimestamp(now.minusDays(7));
                log2.setPerformedBy(admin1);

                AuditLog log3 = new AuditLog();
                log3.setAction("APPOINTMENT_CREATED");
                log3.setDetails("Appointment scheduled between " + client1.getFirstName() + " and " + provider1.getFirstName());
                log3.setTimestamp(now.minusDays(6));
                log3.setPerformedBy(admin1);

                AuditLog log4 = new AuditLog();
                log4.setAction("APPOINTMENT_COMPLETED");
                log4.setDetails("Appointment completed successfully");
                log4.setTimestamp(now.minusDays(5));
                log4.setPerformedBy(provider1);

                AuditLog log5 = new AuditLog();
                log5.setAction("USER_STATUS_CHANGED");
                log5.setDetails("User status changed to inactive: " + client6.getEmail());
                log5.setTimestamp(now.minusDays(4));
                log5.setPerformedBy(admin2);

                AuditLog log6 = new AuditLog();
                log6.setAction("APPOINTMENT_CANCELLED");
                log6.setDetails("Appointment cancelled by client: " + client1.getFirstName());
                log6.setTimestamp(now.minusDays(1));
                log6.setPerformedBy(client1);

                List<AuditLog> auditLogs = Arrays.asList(log1, log2, log3, log4, log5, log6);
                auditLogRepository.saveAll(auditLogs);
                System.out.println("Created " + auditLogs.size() + " audit logs");

                System.out.println("===========================================");
                System.out.println("Sample data initialization completed!");
                System.out.println("Summary:");
                System.out.println("- Admin users: 2");
                System.out.println("- Provider users: 4 (3 active, 1 inactive)");
                System.out.println("- Client users: 8 (7 active, 1 inactive)");
                System.out.println("- Total appointments: 10");
                System.out.println("- Audit logs: 6");
                System.out.println("===========================================");
            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
        };
    }
}