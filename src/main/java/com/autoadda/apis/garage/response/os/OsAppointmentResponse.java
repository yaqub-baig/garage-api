package com.autoadda.apis.garage.response;

import java.util.List;

import com.autoadda.apis.garage.entity.os.OsAppointmentDetails;
import com.autoadda.apis.garage.entity.os.OsAppointmentServices;
import com.autoadda.apis.garage.entity.os.OsJobCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
