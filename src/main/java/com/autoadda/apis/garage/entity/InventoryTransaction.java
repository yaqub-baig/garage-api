package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "garage_inventory_item_id", nullable = false)
    private GarageInventoryItem garageInventoryItem;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;  // Positive for additions, negative for removals

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "total_value")
    private Double totalValue;  // quantityChange * unitPrice

    @ManyToOne
    @JoinColumn(name = "performed_by_technician_id")
    private Technician performedBy;

    @Column(name = "reference_type")
    private String referenceType;  // e.g., "JOB_CARD", "WORK_ORDER", "MANUAL_ADJUSTMENT"

    @Column(name = "reference_id")
    private Integer referenceId;  // ID of the related entity

    @Column(name = "supplier_name")
    private String supplierName;  // For STOCK_IN transactions

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "reason", length = 1000)
    private String reason;  // Required for ADJUSTMENT, DAMAGE, RETURN

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (unitPrice != null && quantityChange != null) {
            totalValue = Math.abs(quantityChange) * unitPrice;
        }
    }
}
