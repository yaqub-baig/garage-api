package com.autoadda.apis.garage.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailResponse {
    private String id;              // UUID from Part API
    private String name;            // Part name (e.g., "Clutch Kit")
    private String description;     // Detailed description
    private String partNumber;      // Part number (e.g., "QA21034DFT")

    // Vehicle information
    private String year;
    private String make;
    private String model;
    private String tag;
    private String otherNames;

    // Category and Manufacturer IDs
    private String categoryId;
    private String manufacturerId;
    private String attributeUrl;

    // Nested objects
    private CategoryResponse category;        // Category object
    private List<ProductAttributeResponse> attributes;
    private List<ProductImageResponse> images;

    // Pricing
    private Double unitPrice;       // Default price from catalog
    private String productCondition; // NEW, USED, REFURBISHED

    // Additional fields from Part API
    private String supplierId;
    private String supplierName;

    // Helper methods for backward compatibility
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public String getManufacturer() {
        // This can be populated from manufacturerId or other source if needed
        return manufacturerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryResponse {
        private String id;
        private String name;
        private String code;
        private String parentCategoryId;
        private List<CategoryResponse> subCategories;
        private String attributeUrl;
        private List<Object> images;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductImageResponse {
        private Long id;
        private String fileName;
        private String originalFileName;
        private String filePath;
        private String mediaType;
        private Long size;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductAttributeResponse {
        private String id;
        private String name;
        private String value;
    }
}
