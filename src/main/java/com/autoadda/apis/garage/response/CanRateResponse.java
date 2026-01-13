package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanRateResponse {
    private Boolean canRate;
    private String reason;
    private Integer workOrderId;
    private Integer garageId;
}
