package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ProductCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageInventoryItemResponse {
    private Integer id;
    private Integer garageId;
    private String inventoryPartId;
    private String partName;
    private String partNumber;
    private String sku;
    private Integer quantityInStock;
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
    private Integer reorderQuantity;
    private String shelfLocation;
    private String binLocation;
    private Double unitPrice;
    private Double totalValue;  // quantityInStock * unitPrice
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean isOverStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRestockedAt;
    private ProductCondition productCondition;
    private String notes;

    // Part image information
    private Long partImageId;
    private String partImageUrl;
    private String partImageFileName;
}
