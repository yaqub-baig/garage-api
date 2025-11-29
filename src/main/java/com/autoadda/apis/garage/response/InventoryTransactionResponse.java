package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionResponse {
    private Integer id;
    private Integer inventoryItemId;
    private String partName;
    private TransactionType transactionType;
    private Integer quantityChange;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private Double unitPrice;
    private Double totalValue;
    private Integer performedByTechnicianId;
    private String performedByTechnicianName;
    private String referenceType;
    private Integer referenceId;
    private String supplierName;
    private String receiptNumber;
    private String reason;
    private String notes;
    private LocalDateTime transactionDate;
}
