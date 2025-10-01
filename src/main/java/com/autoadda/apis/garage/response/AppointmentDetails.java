package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentDetails {

    private Integer id;

    private LocalDate scheduleDate;

    private LocalTime serviceStartTime;

    private LocalTime serviceEndTime;

    private LocalTime deliveryTime;

    private Status status;

    private String comments;

    private LocalDate insert_tms;

    private Garage garage;

    private Customer customer;

    private VehicleDetails vehicle;

}
