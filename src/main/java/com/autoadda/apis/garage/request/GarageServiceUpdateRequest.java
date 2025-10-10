package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GarageServiceUpdateRequest {
private Boolean isActive;
private Double labourPrice;
private Double price;
}
