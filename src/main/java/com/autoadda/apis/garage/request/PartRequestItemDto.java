package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestItemDto {

    private String partName;
    private String partNumber;
    private Integer quantity;
    private String description;
    private Double estimatedPrice;
}
