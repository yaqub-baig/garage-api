package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<AppointmentSlot, Integer> {
    List<AppointmentSlot> findByStartTimeAndEndTimeAndCurrentBookingsLessThanMaxBookings(
            LocalTime startTime, LocalTime endTime);

}
