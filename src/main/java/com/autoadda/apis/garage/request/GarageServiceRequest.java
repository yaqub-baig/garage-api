package com.autoadda.apis.garage.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageServiceRequest {

    @NotNull(message = "Garage ID is required")
    private Integer garageId;

    @NotNull(message = "Service ID is required")
    private Integer serviceId;

    private boolean isActive;
}
