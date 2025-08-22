package com.autoadda.apis.garage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalTime;
@Entity
@Data
public class AppointmentSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    private Integer currentBookings;
    public boolean isAvailable() {
        return currentBookings < maxBookings;
    }

    // Helper method to get available bookings count
    public Integer getAvailableBookings() {
        return maxBookings - currentBookings;
    }
}
