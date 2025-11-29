package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockRequest {
    private Integer inventoryItemId;
    private Integer quantity;
    private Double unitPrice;
    private Integer technicianId;
    private String supplierName;
    private String receiptNumber;
    private String notes;
}
