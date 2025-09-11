package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentServiceResponse {
    private Integer id;
    private Status status;
    private Integer appointmentId;
    private Integer serviceId;
    private Integer technicianId;

}
