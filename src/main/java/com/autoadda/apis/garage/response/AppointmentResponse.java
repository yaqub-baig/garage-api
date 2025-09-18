package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.JobCard;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentResponse {
    private AppointmentDetails appointment;
    private List<AppointmentServices> services;
    private List<JobCard> jobcards;

}
