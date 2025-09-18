package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentServices {

    private Integer id;

    private Status status;

    private Integer appointmentId;

    private LocalDate insert_tms;

    private GarageService service;

    private GarageTechnician technician;

}
