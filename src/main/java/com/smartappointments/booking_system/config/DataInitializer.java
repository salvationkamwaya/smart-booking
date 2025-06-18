package com.smartappointments.booking_system.config;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
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
                provider4.setStatus(User.Status.INACTIVE); // Inactive provider

                // Create Client Users
                User client1 = new User();
                client1.setEmail("john.doe@email.com");
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

                User client5 = new User();
                client5.setEmail("charlie.davis@email.com");
                client5.setFirstName("Charlie");
                client5.setLastName("Davis");
                client5.setRole(User.Role.CLIENT);
                client5.setPassword(passwordEncoder.encode("dawson123")); // Different password for variety
                client5.setStatus(User.Status.ACTIVE);

                User client6 = new User();
                client6.setEmail("diana.miller@email.com");
                client6.setFirstName("Diana");
                client6.setLastName("Miller");
                client6.setRole(User.Role.CLIENT);
                client6.setPassword(defaultPassword);
                client6.setStatus(User.Status.INACTIVE); // Inactive client

                User client7 = new User();
                client7.setEmail("edward.garcia@email.com");
                client7.setFirstName("Edward");
                client7.setLastName("Garcia");
                client7.setRole(User.Role.CLIENT);
                client7.setPassword(defaultPassword);
                client7.setStatus(User.Status.PENDING); // Pending client

                User client8 = new User();
                client8.setEmail("fiona.rodriguez@email.com");
                client8.setFirstName("Fiona");
                client8.setLastName("Rodriguez");
                client8.setRole(User.Role.CLIENT);
                client8.setPassword(defaultPassword);
                client8.setStatus(User.Status.ACTIVE);

                // Save all users
                List<User> users = Arrays.asList(
                    admin1, admin2, provider1, provider2, provider3, provider4,
                    client1, client2, client3, client4, client5, client6, client7, client8
                );

                userRepository.saveAll(users);
                System.out.println("Sample data initialized successfully!");
                System.out.println("Default login credentials:");
                System.out.println("Admin: admin@admin.com / password123");
                System.out.println("Provider: dr.johnson@hospital.com / password123");
                System.out.println("Client: john.doe@email.com / password123");
            }
        };
    }
}
