package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.ProductCondition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "garage_inventory_item",
       uniqueConstraints = @UniqueConstraint(columnNames = {"garage_id", "inventory_part_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageInventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "inventory_part_id")
    private String inventoryPartId;  // UUID from Part API (if available)

    // Garage-specific stock information
    @Column(name = "quantity_in_stock", nullable = false)
    @Builder.Default
    private Integer quantityInStock = 0;

    @Column(name = "minimum_stock_level")
    private Integer minimumStockLevel;  // Reorder threshold

    @Column(name = "maximum_stock_level")
    private Integer maximumStockLevel;

    @Enumerated(EnumType.STRING)
    private ProductCondition productCondition;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;  // Suggested quantity to order

    @Column(name = "shelf_location")
    private String shelfLocation;  // Garage-specific location

    @Column(name = "bin_location")
    private String binLocation;

    // Cached data from InventoryPart for quick access
    @Column(name = "part_name")
    private String partName;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "sku")
    private String sku;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToOne
    @JoinColumn(name = "part_image_id")
    private MediaFile partImage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantityInStock == null) {
            quantityInStock = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic helper
    public boolean isLowStock() {
        return minimumStockLevel != null && quantityInStock <= minimumStockLevel;
    }

    public boolean isOverStock() {
        return maximumStockLevel != null && quantityInStock >= maximumStockLevel;
    }
}
