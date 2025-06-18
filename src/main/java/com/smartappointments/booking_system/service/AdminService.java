package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserService userService;

    public Page<User> searchUsers(String search, String role, String status, Pageable pageable) {
        User.Role roleEnum = null;
        User.Status statusEnum = null;

        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid role, keep as null
            }
        }

        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = User.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, keep as null
            }
        }

        return userService.getUsers(roleEnum, statusEnum, search, pageable);
    }

    @Transactional
    public User createUser(User user) {
        return userService.createUser(user);
    }

    @Transactional
    public User updateUser(Long id, User user) {
        return userService.updateUser(id, user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    @Transactional
    public boolean updateUserStatus(Long userId, boolean active) {
        User.Status newStatus = active ? User.Status.ACTIVE : User.Status.INACTIVE;
        User updatedUser = userService.updateUserStatus(userId, newStatus);
        return updatedUser.getStatus() == User.Status.ACTIVE;
    }

    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    // Statistics methods
    public long getTotalUsers() {
        return userService.countByRoleAndStatus(null, User.Status.ACTIVE) +
               userService.countByRoleAndStatus(null, User.Status.INACTIVE) +
               userService.countByRoleAndStatus(null, User.Status.PENDING);
    }

    public long getActiveUsers() {
        return userService.countByRoleAndStatus(null, User.Status.ACTIVE);
    }

    public long getPendingUsers() {
        return userService.countByRoleAndStatus(null, User.Status.PENDING);
    }

    public long getUsersByRole(User.Role role) {
        return userService.countByRoleAndStatus(role, User.Status.ACTIVE);
    }
}