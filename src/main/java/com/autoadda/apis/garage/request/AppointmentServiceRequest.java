package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;
import lombok.Data;

@Data
public class AppointmentServiceRequest {
    private Status status;
    private Integer appointmentId;
    private Integer serviceId;
    private Integer technicianId;
}
