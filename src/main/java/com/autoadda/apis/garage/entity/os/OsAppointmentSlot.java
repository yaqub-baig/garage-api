package com.autoadda.apis.garage.entity.os;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Getter
@Setter
public class OsAppointmentSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    @ManyToOne
    @JoinColumn(name = "otherServiceId")
    private OtherService otherService;
    private Integer currentBookings;
    public boolean isAvailable() {
        return currentBookings < maxBookings;
    }

    // Helper method to get available bookings count
    public Integer getAvailableBookings() {
        return maxBookings - currentBookings;
    }
}
