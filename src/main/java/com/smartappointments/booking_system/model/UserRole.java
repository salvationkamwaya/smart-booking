package com.smartappointments.booking_system.model;

public enum UserRole {
    ROLE_ADMIN,
    ROLE_PROVIDER,
    ROLE_CLIENT,  // Add this to match your database values
    CLIENT        // Add this if some records use just "CLIENT" without "ROLE_" prefix
}