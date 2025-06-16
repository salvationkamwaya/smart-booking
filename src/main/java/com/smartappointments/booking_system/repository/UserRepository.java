package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(String role);
}

