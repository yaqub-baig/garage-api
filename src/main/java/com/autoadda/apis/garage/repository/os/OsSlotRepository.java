package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.entity.os.OsAppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OsSlotRepository extends JpaRepository<OsAppointmentSlot, Integer> {

    List<OsAppointmentSlot> findByOtherServiceIdAndSlotDateGreaterThanEqual(Integer otherServiceId, LocalDate date);


    // Find slots within a time range
    List<OsAppointmentSlot> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    // Find available slots within a time range (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.startTime BETWEEN :startTime AND :endTime AND s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlotsBetween(@Param("startTime") LocalTime startTime,
                                                    @Param("endTime") LocalTime endTime);

    // Find all available slots (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlots();

    // Find slots by start time
    List<OsAppointmentSlot> findByStartTime(LocalTime startTime);

    // Find available slots by start time (currentBookings < maxBookings)
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.startTime = :startTime AND s.currentBookings < s.maxBookings")
    List<OsAppointmentSlot> findAvailableSlotsByStartTime(@Param("startTime") LocalTime startTime);

    Optional<OsAppointmentSlot> findBySlotDateAndStartTime(LocalDate date, LocalTime currentTime);

    List<OsAppointmentSlot> findByOtherServiceIdAndSlotDate(Integer otherServiceId, LocalDate date);

    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.otherService.id = :otherServiceId AND s.slotDate= :slotDate AND s.startTime = :time AND s.currentBookings < s.maxBookings")
    Optional<OsAppointmentSlot> findByOtherServiceAndStartTime(@Param("otherServiceId") Integer otherServiceId, @Param("slotDate") LocalDate date, @Param("time") LocalTime time);
    @Query("SELECT s FROM OsAppointmentSlot s WHERE s.otherService.id = :otherServiceId " +
            "AND s.slotDate >= :startDate AND s.slotDate <= :endDate " +
            "ORDER BY s.slotDate, s.startTime")
    List<OsAppointmentSlot> findMonthlySlots(
            @Param("otherServiceId") Integer garageId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<OsAppointmentSlot> findAvailableSlotsByOtherServiceId(Integer otherServiceId);

    Optional<OsAppointmentSlot> findByOtherServiceIdAndSlotDateAndStartTime(Integer id, LocalDate date, LocalTime currentTime);
    @Query("SELECT s FROM OsAppointmentSlot s WHERE " +
            "(s.slotDate < :currentDate) OR " +
            "(s.slotDate = :currentDate AND s.endTime <= :currentTime)")
    List<OsAppointmentSlot> findPastSlots(  @Param("currentDate") LocalDate currentDate,
                                          @Param("currentTime") LocalTime currentTime);
}