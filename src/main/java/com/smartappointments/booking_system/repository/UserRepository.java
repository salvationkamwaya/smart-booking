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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "u.firstName LIKE CONCAT('%', :search, '%') OR " +
           "u.lastName LIKE CONCAT('%', :search, '%') OR " +
           "u.email LIKE CONCAT('%', :search, '%'))")
    Page<User> findByFilters(
            @Param("role") Role role,
            @Param("status") Status status,
            @Param("search") String search,
            Pageable pageable
    );    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.status = :status")
    long countByRoleAndStatus(@Param("role") Role role, @Param("status") Status status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") Status status);
      // Find users by role
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") Role role);
    
    // Find users created between dates
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}