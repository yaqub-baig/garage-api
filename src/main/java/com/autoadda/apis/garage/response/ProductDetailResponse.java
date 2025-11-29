package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailResponse {
    private String id;              // UUID from Part API
    private String name;            // Part name (e.g., "Clutch Kit")
    private String description;     // Detailed description
    private String partNumber;      // Part number (e.g., "QA21034DFT")
    private String sku;             // Stock Keeping Unit
    private String category;        // Part category
    private String manufacturer;    // Manufacturer name
    private Double unitPrice;       // Default price from catalog
    private String productCondition; // NEW, USED, REFURBISHED

    // Additional fields from Part API
    private String supplierId;
    private String supplierName;
}
