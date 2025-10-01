package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GarageServiceResponse {
    private Integer id;
    private Boolean isActive;
    private Integer garageId;
    private Double price;
    private Double laborPrice;
    private AvailableServiceResponse availableService; // Using the existing AvailableServiceResponse
}
