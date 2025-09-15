package com.autoadda.apis.garage.request.os;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Data
public class OsAppointmentSlotRequest {
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
    private Integer maxBookings;
    private Integer currentBookings;
}
