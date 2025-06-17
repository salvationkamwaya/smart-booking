package com.smartappointments.booking_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    // Define the UserRole enum as an inner class
    public enum UserRole {
        ROLE_ADMIN,
        ROLE_PROVIDER,
        ROLE_CLIENT;

        public static UserRole fromString(String role) {
            try {
                return UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private String password;

    // Constructors
    public User() {
    }

    public User(String email, String firstName, String lastName,
                UserRole role, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.password = password;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // Convenience method for string roles
    public void setRoleFromString(String role) {
        this.role = UserRole.fromString(role);
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}