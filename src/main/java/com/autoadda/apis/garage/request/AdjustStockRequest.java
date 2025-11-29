package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustStockRequest {
    private Integer inventoryItemId;
    private Integer newQuantity;
    private Integer technicianId;
    private String adjustmentReason;  // "DAMAGE", "COUNT_CORRECTION", "RETURN"
    private String notes;
}
