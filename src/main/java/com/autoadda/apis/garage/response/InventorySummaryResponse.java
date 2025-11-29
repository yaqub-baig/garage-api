package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySummaryResponse {
    private Integer garageId;
    private Long totalItems;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Long overstockedItems;
    private Double totalInventoryValue;
    private Long activeAlertsCount;
}
