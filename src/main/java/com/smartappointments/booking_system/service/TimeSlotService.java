package com.smartappointments.booking_system.service;

import com.smartappointments.booking_system.model.TimeSlot;
import com.smartappointments.booking_system.model.User;
import com.smartappointments.booking_system.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class TimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Transactional
    public TimeSlot createSingleSlot(User provider, LocalDate date, LocalTime startTime, Integer duration, Integer maxAttendees, String notes) {
        // Check if slot already exists
        if (timeSlotRepository.existsByProviderAndDateAndStartTime(provider, date, startTime)) {
            throw new RuntimeException("Time slot already exists for this date and time");
        }

        LocalTime endTime = startTime.plusMinutes(duration);
        
        TimeSlot timeSlot = new TimeSlot(provider, date, startTime, endTime, duration);
        timeSlot.setMaxAttendees(maxAttendees);
        timeSlot.setNotes(notes);
        
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional
    public List<TimeSlot> createRecurringSlots(User provider, LocalDate startDate, LocalDate endDate, 
                                             LocalTime startTime, LocalTime endTime, Set<String> daysOfWeek,
                                             Integer maxAttendees, String notes) {
        List<TimeSlot> createdSlots = new ArrayList<>();
        Integer duration = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        
        // Generate slots for each day in the date range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dayOfWeek = currentDate.getDayOfWeek().toString();
            
            if (daysOfWeek.contains(dayOfWeek)) {
                // Check if slot already exists
                if (!timeSlotRepository.existsByProviderAndDateAndStartTime(provider, currentDate, startTime)) {
                    TimeSlot timeSlot = new TimeSlot(provider, currentDate, startTime, endTime, duration);
                    timeSlot.setMaxAttendees(maxAttendees);
                    timeSlot.setNotes(notes);
                    timeSlot.setRecurring(true);
                    timeSlot.setRecurringStartDate(startDate);
                    timeSlot.setRecurringEndDate(endDate);
                    timeSlot.setDaysOfWeek(daysOfWeek);
                    
                    createdSlots.add(timeSlotRepository.save(timeSlot));
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return createdSlots;
    }

    public List<TimeSlot> getProviderSlots(User provider) {
        return timeSlotRepository.findByProviderOrderByDateAscStartTimeAsc(provider);
    }

    public List<TimeSlot> getProviderSlotsForDate(User provider, LocalDate date) {
        return timeSlotRepository.findByProviderAndDateOrderByStartTime(provider, date);
    }

    public List<TimeSlot> getAvailableSlots(User provider, LocalDate fromDate) {
        return timeSlotRepository.findAvailableSlotsByProviderFromDate(provider, fromDate);
    }

    public List<TimeSlot> getAvailableSlotsForDate(User provider, LocalDate date) {
        return timeSlotRepository.findAvailableSlotsByProviderAndDate(provider, date);
    }    // Get available slots by date range
    public List<TimeSlot> getAvailableSlotsByDateRange(LocalDate startDate, LocalDate endDate) {
        return timeSlotRepository.findByDateBetweenAndIsAvailableTrueOrderByDateAscStartTimeAsc(
            startDate, endDate);
    }

    // Get available slots by provider and date range
    public List<TimeSlot> getAvailableSlotsByProviderAndDateRange(User provider, LocalDate startDate, LocalDate endDate) {
        return timeSlotRepository.findByProviderAndDateBetweenAndIsAvailableTrueOrderByDateAscStartTimeAsc(
            provider, startDate, endDate);
    }

    // Get available slots by date
    public List<TimeSlot> getAvailableSlotsByDate(LocalDate date) {
        return timeSlotRepository.findByDateAndIsAvailableTrueOrderByStartTimeAsc(date);
    }

    // Get available slots by provider and date
    public List<TimeSlot> getAvailableSlotsByProviderAndDate(User provider, LocalDate date) {
        return timeSlotRepository.findByProviderAndDateAndIsAvailableTrueOrderByStartTimeAsc(provider, date);
    }

    // Find time slot by ID
    public Optional<TimeSlot> findById(Long id) {
        return timeSlotRepository.findById(id);
    }

    // Increment attendees for a time slot
    public void incrementAttendees(Long timeSlotId) {
        Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findById(timeSlotId);
        if (timeSlotOpt.isPresent()) {
            TimeSlot timeSlot = timeSlotOpt.get();
            timeSlot.setCurrentAttendees(timeSlot.getCurrentAttendees() + 1);
            
            // Mark as unavailable if at capacity
            if (timeSlot.getCurrentAttendees() >= timeSlot.getMaxAttendees()) {
                timeSlot.setIsAvailable(false);
            }
            
            timeSlotRepository.save(timeSlot);
        }
    }

    // Decrement attendees for a time slot (for cancellations)
    public void decrementAttendees(Long timeSlotId) {
        Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findById(timeSlotId);
        if (timeSlotOpt.isPresent()) {
            TimeSlot timeSlot = timeSlotOpt.get();
            if (timeSlot.getCurrentAttendees() > 0) {
                timeSlot.setCurrentAttendees(timeSlot.getCurrentAttendees() - 1);
                timeSlot.setIsAvailable(true); // Make available again
                timeSlotRepository.save(timeSlot);
            }
        }
    }

    @Transactional
    public void deleteTimeSlot(Long slotId, User provider) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
            .orElseThrow(() -> new RuntimeException("Time slot not found"));
        
        if (!timeSlot.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only delete your own time slots");
        }
        
        timeSlotRepository.delete(timeSlot);
    }

    @Transactional
    public TimeSlot updateTimeSlot(Long slotId, User provider, Map<String, Object> updates) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
            .orElseThrow(() -> new RuntimeException("Time slot not found"));
        
        if (!timeSlot.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("You can only update your own time slots");
        }

        // Update fields if provided
        if (updates.containsKey("date")) {
            timeSlot.setDate(LocalDate.parse((String) updates.get("date")));
        }
        if (updates.containsKey("startTime")) {
            timeSlot.setStartTime(LocalTime.parse((String) updates.get("startTime")));
        }
        if (updates.containsKey("endTime")) {
            timeSlot.setEndTime(LocalTime.parse((String) updates.get("endTime")));
        }
        if (updates.containsKey("duration")) {
            timeSlot.setDuration((Integer) updates.get("duration"));
        }
        if (updates.containsKey("maxAttendees")) {
            timeSlot.setMaxAttendees((Integer) updates.get("maxAttendees"));
        }
        if (updates.containsKey("notes")) {
            timeSlot.setNotes((String) updates.get("notes"));
        }
        if (updates.containsKey("status")) {
            timeSlot.setStatus((String) updates.get("status"));
        }

        return timeSlotRepository.save(timeSlot);
    }

    public Set<String> convertDaysToSet(String[] days) {
        Set<String> daysSet = new HashSet<>();
        for (String day : days) {
            daysSet.add(day.toUpperCase());
        }
        return daysSet;
    }

    // Get all time slots
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }
}
