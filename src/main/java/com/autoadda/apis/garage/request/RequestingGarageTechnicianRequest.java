package com.autoadda.apis.garage.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestingGarageTechnicianRequest {

    @NotNull(message = "Garage ID is required")
    private Integer garageId;

    @NotNull(message = "Technician ID is required")
    private Integer technicianId;

    private String description;  // Optional description/message for the request

    // Note: sendBy will be automatically determined by the service layer
    // based on the authenticated user's role (not provided by client)
}
