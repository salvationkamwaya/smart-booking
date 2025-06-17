package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.model.User.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Basic queries
    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    Optional<User> findByEmail(String email);

    // Search functionality
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // Role-based queries (enum version)
    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndIsActive(UserRole role, boolean isActive, Pageable pageable);

    // Updated String-based role queries (fixed syntax)
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRoleString(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = :isActive")
    List<User> findByRoleStringAndIsActive(
            @Param("role") UserRole role,
            @Param("isActive") boolean isActive
    );

    // Count queries for statistics
    long countByRole(UserRole role);

    long countByRoleAndIsActive(UserRole role, boolean isActive);

    // Additional helper methods
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}