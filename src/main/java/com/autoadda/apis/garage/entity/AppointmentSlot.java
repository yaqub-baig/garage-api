package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class AppointmentSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;
    private Integer currentBookings;

    public boolean isAvailable() {
        return currentBookings < maxBookings;
    }

    // Helper method to get available bookings count
    public Integer getAvailableBookings() {
        return maxBookings - currentBookings;
    }
}
