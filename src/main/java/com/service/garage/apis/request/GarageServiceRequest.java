package com.service.garage.apis.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
