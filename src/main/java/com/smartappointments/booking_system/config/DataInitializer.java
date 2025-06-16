package com.smartappointments.booking_system.config;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.UserRole;
import com.smartappointments.booking_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev") // Only runs in dev profile
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Only initialize if database is empty
            if (userRepository.count() == 0) {
                // Create sample users
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(UserRole.ROLE_ADMIN);
                admin.setActive(true);

                User provider = new User();
                provider.setEmail("provider@example.com");
                provider.setFirstName("Dr. Sarah");
                provider.setLastName("Johnson");
                provider.setRole(UserRole.ROLE_PROVIDER);
                provider.setActive(true);

                User client = new User();
                client.setEmail("client@example.com");
                client.setFirstName("John");
                client.setLastName("Doe");
                client.setRole(UserRole.ROLE_CLIENT);
                client.setActive(true);

                // Save to database
                userRepository.save(admin);
                userRepository.save(provider);
                userRepository.save(client);

                System.out.println("Sample data initialized successfully!");
            }
        };
    }
}