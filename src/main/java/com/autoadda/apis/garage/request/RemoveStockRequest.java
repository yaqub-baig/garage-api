package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveStockRequest {
    private Integer inventoryItemId;
    private Integer quantity;
    private Integer technicianId;
    private String referenceType;  // "JOB_CARD", "WORK_ORDER", "SALE"
    private Integer referenceId;
    private String notes;
}
