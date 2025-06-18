package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;    @BeforeEach
    void setUp() {
        testUser = new User("john.doe@example.com", "John", "Doe", User.Role.CLIENT, "hashedPassword");
        // For testing purposes, we'll simulate the user having an ID as if it was saved to database
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, 1L);
        } catch (Exception e) {
            // In case reflection fails, we can still test most functionality
        }
    }

    @Test
    void testGetUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        
        when(userRepository.findByFilters(any(), any(), anyString(), any(Pageable.class)))
                .thenReturn(userPage);

        // When
        Page<User> result = userService.getUsers(User.Role.CLIENT, User.Status.ACTIVE, "john", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(userRepository).findByFilters(User.Role.CLIENT, User.Status.ACTIVE, "john", pageable);
    }

    @Test
    void testCreateUser_Success() {
        // Given
        User newUser = new User();
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setRole(User.Role.PROVIDER);
        newUser.setPassword("plainPassword");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(newUser);
    }

    @Test
    void testCreateUser_EmailExists() {
        // Given
        User newUser = new User();
        newUser.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.createUser(newUser));
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }    @Test
    void testUpdateUser_Success() {
        // Given
        Long userId = 1L;
        User updateData = new User();
        updateData.setFirstName("Updated");
        updateData.setLastName("Name");
        updateData.setEmail("updated@example.com");
        updateData.setRole(User.Role.ADMIN);
        updateData.setStatus(User.Status.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(userId, updateData);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateUserStatus() {
        // Given
        Long userId = 1L;
        User.Status newStatus = User.Status.INACTIVE;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserStatus(userId, newStatus);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> userService.getUserById(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testCountByRoleAndStatus() {
        // Given
        when(userRepository.countByRoleAndStatus(User.Role.CLIENT, User.Status.ACTIVE))
                .thenReturn(5L);

        // When
        long count = userService.countByRoleAndStatus(User.Role.CLIENT, User.Status.ACTIVE);

        // Then
        assertEquals(5L, count);
        verify(userRepository).countByRoleAndStatus(User.Role.CLIENT, User.Status.ACTIVE);
    }

    @Test
    void testIsEmailAvailable() {
        // Given
        String email = "available@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean isAvailable = userService.isEmailAvailable(email);

        // Then
        assertTrue(isAvailable);
        verify(userRepository).existsByEmail(email);
    }
}
