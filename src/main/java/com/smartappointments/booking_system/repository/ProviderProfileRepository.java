package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.ProviderProfile;
import com.smartappointments.booking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long> {
    Optional<ProviderProfile> findByUser(User user);
}
