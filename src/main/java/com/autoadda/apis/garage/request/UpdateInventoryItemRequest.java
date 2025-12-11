package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.ProductCondition;
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
    private Integer quantityInStock;
    private Integer reorderQuantity;
    private String shelfLocation;
    private String binLocation;
    private ProductCondition productCondition;
    private String notes;
}
