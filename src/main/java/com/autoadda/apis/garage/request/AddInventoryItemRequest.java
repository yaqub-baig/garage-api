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
public class AddInventoryItemRequest {
    private Integer garageId;

    // Option 1: Search by part number (will auto-populate from Part API)
    private String partNumber;

    // Option 2: Manual entry (fields will be populated if partNumber is provided)
    private String inventoryPartId;  // UUID from Part API (if available)
    private String partName;
    private String sku;
    private String description;
    private String category;
    private String manufacturer;
    private Double unitPrice;

    // Garage-specific settings (always required)
    private ProductCondition productCondition;
    private Integer minimumStockLevel;
    private Integer quantityInStock;
    private Integer maximumStockLevel;
    private Integer reorderQuantity;
    private String shelfLocation;
    private String binLocation;
    private String notes;
}
