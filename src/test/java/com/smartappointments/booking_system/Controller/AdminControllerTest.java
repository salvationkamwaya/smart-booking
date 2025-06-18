package com.smartappointments.booking_system.Controller;

import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setRole(User.Role.CLIENT);
        testUser.setStatus(User.Status.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsersApi() throws Exception {
        // Given
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userService.getUsers(any(), any(), anyString(), any())).thenReturn(userPage);
        when(userService.countByRoleAndStatus(any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/admin/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].firstName").value("John"))
                .andExpect(jsonPath("$.users[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.users[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.counts").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        // Given
        User newUser = new User();
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setRole(User.Role.PROVIDER);
        newUser.setStatus(User.Status.ACTIVE);

        when(userService.createUser(any(User.class))).thenReturn(newUser);

        // When & Then
        mockMvc.perform(post("/admin/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFirstName("John Updated");
        updatedUser.setLastName("Doe Updated");
        updatedUser.setEmail("john.updated@example.com");
        updatedUser.setRole(User.Role.ADMIN);
        updatedUser.setStatus(User.Status.ACTIVE);

        when(userService.updateUser(any(Long.class), any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/admin/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserStatus() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setStatus(User.Status.INACTIVE);

        when(userService.updateUserStatus(1L, User.Status.INACTIVE)).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/admin/api/users/1/status")
                .with(csrf())
                .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/admin/api/users/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsersWithFilters() throws Exception {
        // Given
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userService.getUsers(User.Role.CLIENT, User.Status.ACTIVE, "john", PageRequest.of(0, 10)))
                .thenReturn(userPage);
        when(userService.countByRoleAndStatus(any(), any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/admin/api/users")
                .param("role", "CLIENT")
                .param("status", "ACTIVE")
                .param("search", "john")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].firstName").value("John"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testForbiddenAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isForbidden());
    }
}
