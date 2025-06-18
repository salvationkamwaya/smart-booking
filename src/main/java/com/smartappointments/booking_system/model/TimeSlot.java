package com.smartappointments.booking_system.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration; // in minutes
    private Integer maxAttendees;
    private Integer currentAttendees; // Track current bookings
    private Boolean isAvailable; // Whether slot is available for booking
    private String notes;
    private String status; // AVAILABLE, BOOKED, BLOCKED
    private boolean isRecurring;
    
    // For recurring slots
    private LocalDate recurringStartDate;
    private LocalDate recurringEndDate;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "time_slot_days", joinColumns = @JoinColumn(name = "time_slot_id"))
    @Column(name = "day_of_week")
    private Set<String> daysOfWeek; // MONDAY, TUESDAY, etc.

    // Constructors
    public TimeSlot() {
        this.status = "AVAILABLE";
        this.isRecurring = false;
        this.maxAttendees = 1;
        this.currentAttendees = 0;
        this.isAvailable = true;
    }

    public TimeSlot(User provider, LocalDate date, LocalTime startTime, LocalTime endTime, Integer duration) {
        this();
        this.provider = provider;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public Integer getCurrentAttendees() {
        return currentAttendees;
    }

    public void setCurrentAttendees(Integer currentAttendees) {
        this.currentAttendees = currentAttendees;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public LocalDate getRecurringStartDate() {
        return recurringStartDate;
    }

    public void setRecurringStartDate(LocalDate recurringStartDate) {
        this.recurringStartDate = recurringStartDate;
    }

    public LocalDate getRecurringEndDate() {
        return recurringEndDate;
    }

    public void setRecurringEndDate(LocalDate recurringEndDate) {
        this.recurringEndDate = recurringEndDate;
    }

    public Set<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
}
