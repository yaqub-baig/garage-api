package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<AppointmentSlot, Integer> {

    List<AppointmentSlot> findByGarageIdAndSlotDateGreaterThanEqual(Integer garageId, LocalDate date);


    // Find slots within a time range
    List<AppointmentSlot> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    // Find available slots within a time range (currentBookings < maxBookings)
    @Query("SELECT s FROM AppointmentSlot s WHERE s.startTime BETWEEN :startTime AND :endTime AND s.currentBookings < s.maxBookings")
    List<AppointmentSlot> findAvailableSlotsBetween(@Param("startTime") LocalTime startTime,
                                                    @Param("endTime") LocalTime endTime);

    // Find all available slots (currentBookings < maxBookings)
    @Query("SELECT s FROM AppointmentSlot s WHERE s.currentBookings < s.maxBookings")
    List<AppointmentSlot> findAvailableSlots();

    // Find slots by start time
    List<AppointmentSlot> findByStartTime(LocalTime startTime);

    // Find available slots by start time (currentBookings < maxBookings)
    @Query("SELECT s FROM AppointmentSlot s WHERE s.startTime = :startTime AND s.currentBookings < s.maxBookings")
    List<AppointmentSlot> findAvailableSlotsByStartTime(@Param("startTime") LocalTime startTime);

    Optional<AppointmentSlot> findBySlotDateAndStartTime(LocalDate date, LocalTime currentTime);

    List<AppointmentSlot> findByGarageIdAndSlotDate(Integer garageId, LocalDate date);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.garage.id = :garageId AND s.slotDate= :slotDate AND s.startTime = :time AND s.currentBookings < s.maxBookings")
    Optional<AppointmentSlot> findByGarageIdAndStartTime(@Param("garageId") Integer garageId, @Param("slotDate") LocalDate date, @Param("time") LocalTime time);

    @Query("SELECT s FROM AppointmentSlot s WHERE s.garage.id = :garageId " +
            "AND s.slotDate >= :startDate AND s.slotDate <= :endDate " +
            "ORDER BY s.slotDate, s.startTime")
    List<AppointmentSlot> findMonthlySlots(
            @Param("garageId") Integer garageId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<AppointmentSlot> findAvailableSlotsByGarageId(Integer garageId);

    Optional<AppointmentSlot> findByGarageIdAndSlotDateAndStartTime(Integer id, LocalDate date, LocalTime currentTime);

    @Query("SELECT s FROM AppointmentSlot s WHERE " +
            "(s.slotDate < :currentDate) OR " +
            "(s.slotDate = :currentDate AND s.endTime <= :currentTime)")
    List<AppointmentSlot> findPastSlots(@Param("currentDate") LocalDate currentDate,
                                        @Param("currentTime") LocalTime currentTime);
}