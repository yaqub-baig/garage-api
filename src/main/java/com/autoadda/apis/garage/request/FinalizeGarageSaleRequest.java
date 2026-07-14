package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalizeGarageSaleRequest {
    private Integer buyerId;
    private Double salePrice;
    private String paymentMode;
    private String notes;
}
