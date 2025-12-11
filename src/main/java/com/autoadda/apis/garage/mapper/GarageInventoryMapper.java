package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.GarageInventoryItem;
import com.autoadda.apis.garage.entity.InventoryAlert;
import com.autoadda.apis.garage.entity.InventoryTransaction;
import com.autoadda.apis.garage.response.GarageInventoryItemResponse;
import com.autoadda.apis.garage.response.InventoryAlertResponse;
import com.autoadda.apis.garage.response.InventoryTransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GarageInventoryMapper {

    @Value("${file.base-url}")
    private String baseUrl;

    public GarageInventoryItemResponse entityToResponse(GarageInventoryItem item) {
        if (item == null) {
            return null;
        }

        Double totalValue = 0.0;
        if (item.getQuantityInStock() != null && item.getUnitPrice() != null) {
            totalValue = item.getQuantityInStock() * item.getUnitPrice();
        }

        return GarageInventoryItemResponse.builder()
                .id(item.getId())
                .garageId(item.getGarage() != null ? item.getGarage().getId() : null)
                .inventoryPartId(item.getInventoryPartId())
                .partName(item.getPartName())
                .partNumber(item.getPartNumber())
                .sku(item.getSku())
                .quantityInStock(item.getQuantityInStock())
                .minimumStockLevel(item.getMinimumStockLevel())
                .maximumStockLevel(item.getMaximumStockLevel())
                .reorderQuantity(item.getReorderQuantity())
                .shelfLocation(item.getShelfLocation())
                .binLocation(item.getBinLocation())
                .unitPrice(item.getUnitPrice())
                .totalValue(totalValue)
                .isLowStock(item.isLowStock())
                .isOutOfStock(item.getQuantityInStock() != null && item.getQuantityInStock() == 0)
                .productCondition(item.getProductCondition())
                .isOverStock(item.isOverStock())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .lastRestockedAt(item.getLastRestockedAt())
                .notes(item.getNotes())
                // Add part image information
                .partImageId(item.getPartImage() != null ? item.getPartImage().getId() : null)
                .partImageUrl(item.getPartImage() != null ?
                        item.getPartImage().getFilePath() : null)
                .partImageFileName(item.getPartImage() != null ?
                        item.getPartImage().getFileName() : null)
                .build();
    }

    public InventoryTransactionResponse transactionToResponse(InventoryTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return InventoryTransactionResponse.builder()
                .id(transaction.getId())
                .inventoryItemId(transaction.getGarageInventoryItem() != null ?
                        transaction.getGarageInventoryItem().getId() : null)
                .partName(transaction.getGarageInventoryItem() != null ?
                        transaction.getGarageInventoryItem().getPartName() : null)
                .transactionType(transaction.getTransactionType())
                .quantityChange(transaction.getQuantityChange())
                .quantityBefore(transaction.getQuantityBefore())
                .quantityAfter(transaction.getQuantityAfter())
                .unitPrice(transaction.getUnitPrice())
                .totalValue(transaction.getTotalValue())
                .performedByTechnicianId(transaction.getPerformedBy() != null ?
                        transaction.getPerformedBy().getId() : null)
                .performedByTechnicianName(transaction.getPerformedBy() != null ?
                        transaction.getPerformedBy().getFirstName() : null)
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .supplierName(transaction.getSupplierName())
                .receiptNumber(transaction.getReceiptNumber())
                .reason(transaction.getReason())
                .notes(transaction.getNotes())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    public InventoryAlertResponse alertToResponse(InventoryAlert alert) {
        if (alert == null) {
            return null;
        }

        return InventoryAlertResponse.builder()
                .id(alert.getId())
                .inventoryItemId(alert.getGarageInventoryItem() != null ?
                        alert.getGarageInventoryItem().getId() : null)
                .partName(alert.getGarageInventoryItem() != null ?
                        alert.getGarageInventoryItem().getPartName() : null)
                .partNumber(alert.getGarageInventoryItem() != null ?
                        alert.getGarageInventoryItem().getPartNumber() : null)
                .alertType(alert.getAlertType())
                .alertStatus(alert.getAlertStatus())
                .currentQuantity(alert.getCurrentQuantity())
                .thresholdLevel(alert.getThresholdLevel())
                .suggestedOrderQuantity(alert.getSuggestedOrderQuantity())
                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByTechnicianId(alert.getResolvedBy() != null ?
                        alert.getResolvedBy().getId() : null)
                .resolvedByTechnicianName(alert.getResolvedBy() != null ?
                        alert.getResolvedBy().getFirstName() : null)
                .resolutionNotes(alert.getResolutionNotes())
                .build();
    }

    /**
     * Construct full URL for image file
     */
    private String constructImageUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return String.format("%s/uploads/%s", baseUrl, filePath);
    }
}
