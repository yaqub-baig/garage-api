package com.autoadda.apis.garage.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentSlotResponse {
    private Integer id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxBookings;
    private Integer currentBookings;
    private Integer availableBookings;
    private boolean available;
    private Integer garageId;

}
