package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentServiceResponse {
    private Integer id;
    private Status status;
    private Integer appointmentId;
    private GarageServiceResponse service;
    private TechnicianResponse technician;
    private Double price;
    private Double labourPrice;

}
