package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryItemRequest {
    private Double unitPrice;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private Integer reorderQuantity;
    private String shelfLocation;
    private String binLocation;
    private String notes;
}
