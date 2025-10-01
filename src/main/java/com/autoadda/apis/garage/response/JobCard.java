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
public class JobCard {

    private Integer id;

    private LocalTime serviceStartTime;

    private LocalTime serviceEndTime;

    private Status status;

    private LocalDate insert_tms;

    private AppointmentDetails appointment;

    private GarageTechnician technician;

    private AppointmentServices service;

    private Garage garage;

    private String comments;

}
