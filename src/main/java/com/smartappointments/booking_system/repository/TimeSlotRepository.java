package com.smartappointments.booking_system.repository;

import com.smartappointments.booking_system.model.TimeSlot;
import com.smartappointments.booking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByProviderAndDateOrderByStartTime(User provider, LocalDate date);
    
    List<TimeSlot> findByProviderAndDateBetweenOrderByDateAscStartTimeAsc(User provider, LocalDate startDate, LocalDate endDate);
    
    List<TimeSlot> findByProviderOrderByDateAscStartTimeAsc(User provider);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.provider = :provider AND ts.date >= :date AND ts.status = 'AVAILABLE' ORDER BY ts.date ASC, ts.startTime ASC")
    List<TimeSlot> findAvailableSlotsByProviderFromDate(@Param("provider") User provider, @Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.provider = :provider AND ts.date = :date AND ts.status = 'AVAILABLE' ORDER BY ts.startTime ASC")
    List<TimeSlot> findAvailableSlotsByProviderAndDate(@Param("provider") User provider, @Param("date") LocalDate date);
    
    boolean existsByProviderAndDateAndStartTime(User provider, LocalDate date, java.time.LocalTime startTime);
    
    // Client booking methods
    List<TimeSlot> findByDateBetweenAndIsAvailableTrueOrderByDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);
    
    List<TimeSlot> findByProviderAndDateBetweenAndIsAvailableTrueOrderByDateAscStartTimeAsc(User provider, LocalDate startDate, LocalDate endDate);
    
    List<TimeSlot> findByDateAndIsAvailableTrueOrderByStartTimeAsc(LocalDate date);
    
    List<TimeSlot> findByProviderAndDateAndIsAvailableTrueOrderByStartTimeAsc(User provider, LocalDate date);
}
