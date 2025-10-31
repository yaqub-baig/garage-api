package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestItemResponse {

    private Integer id;
    private String partName;
    private String partNumber;
    private Integer quantity;
    private String description;
    private Double estimatedPrice;
    private GarageProcurementResponse procurement;
}
