package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AppointmentDetailsRequest {

    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private Date deliveryTime;
    private Status status;
    private String comments;
    private LocalDate insertTms;
    private Integer garageId;

}
