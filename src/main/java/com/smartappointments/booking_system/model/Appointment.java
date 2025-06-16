package com.smartappointments.booking_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity

@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User client;

    @ManyToOne
    private User provider;

    private LocalDateTime appointmentTime;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED


    public Appointment() {
    }

    public Appointment(Long id, User client, User provider, LocalDateTime appointmentTime, String status) {
        this.id = id;
        this.client = client;
        this.provider = provider;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}