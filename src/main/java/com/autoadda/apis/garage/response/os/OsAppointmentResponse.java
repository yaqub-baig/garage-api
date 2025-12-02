package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.os.OsAppointmentDetails;
import com.autoadda.apis.garage.entity.os.OsAppointmentServices;
import com.autoadda.apis.garage.entity.os.OsJobCard;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OsAppointmentResponse {
    private OsAppointmentDetails appointment;
    private List<OsAppointmentServices> services;
    private List<OsJobCard> jobcards;

}
