package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.User.Role;
import com.smartappointments.booking_system.model.User.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findByFilters(
            @Param("role") Role role,
            @Param("status") Status status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = :status")
    long countByRoleAndStatus(@Param("role") Role role, @Param("status") Status status);
}