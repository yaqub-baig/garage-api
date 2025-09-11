package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class VehicleReceivedRequest {
    private Integer vehicleId;
    private Integer appointmentId;
    private Integer receiverId;
    private List<Long> vehicleImages;
    private String comment;
}