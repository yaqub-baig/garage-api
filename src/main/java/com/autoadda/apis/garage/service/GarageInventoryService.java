package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.PartClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageInventoryMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.AlertStatus;
import com.autoadda.apis.garage.util.AlertType;
import com.autoadda.apis.garage.util.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarageInventoryService {

    public static final String INVENTORY_ITEM_NOT_FOUND = "Inventory item not found";
    public static final String TECHNICIAN_NOT_FOUND = "Technician not found";
    private final GarageInventoryItemRepository inventoryItemRepo;
    private final InventoryTransactionRepository transactionRepo;
    private final InventoryAlertRepository alertRepo;
    private final GarageRepository garageRepo;
    private final TechnicianRepository technicianRepo;
    private final GarageInventoryMapper mapper;
    private final PartClient partClient;

    /**
     * Add a new part to garage inventory
     */
    @Transactional
    public GarageInventoryItemResponse addInventoryItem(AddInventoryItemRequest request) {
        log.info("Adding inventory item for garage: {}", request.getGarageId());

        Garage garage = garageRepo.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));

        // NEW: If partNumber is provided, fetch from Part API
        if (request.getPartNumber() != null && !request.getPartNumber().trim().isEmpty()) {
            try {
                ProductResponse productResponse = partClient.getProductByPartNumber(request.getPartNumber());

                if (productResponse != null && "success".equals(productResponse.getStatus())
                        && productResponse.getBody() != null) {

                    ProductDetailResponse product = productResponse.getBody();

                    // Auto-populate fields from Part API
                    request.setInventoryPartId(product.getId());
                    request.setPartName(product.getName());
                    request.setSku(product.getSku());
                    request.setDescription(product.getDescription());
                    request.setCategory(product.getCategory());
                    request.setManufacturer(product.getManufacturer());

                    // Use Part API price if not provided by user
                    if (request.getUnitPrice() == null) {
                        request.setUnitPrice(product.getUnitPrice());
                    }

                    log.info("Successfully fetched part details from Part API for part number: {}",
                            request.getPartNumber());
                } else {
                    log.warn("Part API returned unsuccessful response for part number: {}",
                            request.getPartNumber());
                }
            } catch (Exception e) {
                log.error("Failed to fetch part from Part API for part number: {}. Error: {}",
                        request.getPartNumber(), e.getMessage());
                // Continue with manual entry - optional validation
            }
        }

        // Validate required fields (after API fetch or manual entry)
        if (request.getPartName() == null || request.getPartName().trim().isEmpty()) {
            throw new IllegalArgumentException("Part name is required");
        }

        // Check for duplicates
        if (request.getInventoryPartId() != null) {
            inventoryItemRepo.findByGarageIdAndInventoryPartId(
                            request.getGarageId(), request.getInventoryPartId())
                    .ifPresent(item -> {
                        throw new IllegalStateException("Inventory item already exists for this part");
                    });
        }

        // Validate threshold levels
        if (request.getMinimumStockLevel() != null && request.getMaximumStockLevel() != null && request.getMinimumStockLevel() >= request.getMaximumStockLevel()) {
            throw new IllegalArgumentException("Minimum stock level must be less than maximum stock level");
        }


        GarageInventoryItem item = GarageInventoryItem.builder()
                .garage(garage)
                .inventoryPartId(request.getInventoryPartId())
                .partName(request.getPartName())
                .partNumber(request.getPartNumber())
                .sku(request.getSku())
                .unitPrice(request.getUnitPrice())
                .quantityInStock(0)
                .minimumStockLevel(request.getMinimumStockLevel())
                .maximumStockLevel(request.getMaximumStockLevel())
                .reorderQuantity(request.getReorderQuantity())
                .shelfLocation(request.getShelfLocation())
                .binLocation(request.getBinLocation())
                .notes(request.getNotes())
                .build();

        item = inventoryItemRepo.save(item);
        log.info("Inventory item created with ID: {}", item.getId());

        return mapper.entityToResponse(item);
    }

    /**
     * Get a specific inventory item by ID
     */
    public GarageInventoryItemResponse getInventoryItem(Integer itemId) {
        log.info("Fetching inventory item: {}", itemId);

        GarageInventoryItem item = inventoryItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        return mapper.entityToResponse(item);
    }

    /**
     * Update inventory item details (thresholds, location, etc.)
     */
    @Transactional
    public GarageInventoryItemResponse updateInventoryItem(Integer itemId, UpdateInventoryItemRequest request) {
        log.info("Updating inventory item: {}", itemId);

        GarageInventoryItem item = inventoryItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        // Update fields if provided
        if (request.getUnitPrice() != null) {
            item.setUnitPrice(request.getUnitPrice());
        }
        if (request.getMinimumStockLevel() != null) {
            item.setMinimumStockLevel(request.getMinimumStockLevel());
        }
        if (request.getMaximumStockLevel() != null) {
            item.setMaximumStockLevel(request.getMaximumStockLevel());
        }
        if (request.getReorderQuantity() != null) {
            item.setReorderQuantity(request.getReorderQuantity());
        }
        if (request.getShelfLocation() != null) {
            item.setShelfLocation(request.getShelfLocation());
        }
        if (request.getBinLocation() != null) {
            item.setBinLocation(request.getBinLocation());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }

        // Validate threshold levels
        if (item.getMinimumStockLevel() != null && item.getMaximumStockLevel() != null && item.getMinimumStockLevel() >= item.getMaximumStockLevel()) {
            throw new IllegalArgumentException("Minimum stock level must be less than maximum stock level");
        }


        item = inventoryItemRepo.save(item);
        log.info("Inventory item updated: {}", itemId);

        return mapper.entityToResponse(item);
    }

    /**
     * Add stock to an inventory item
     */
    @Transactional
    public GarageInventoryItemResponse addStock(AddStockRequest request) {
        log.info("Adding stock for inventory item: {}", request.getInventoryItemId());

        // Validate request
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than 0");
        }

        GarageInventoryItem item = inventoryItemRepo.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND));

        int quantityBefore = item.getQuantityInStock();
        int quantityAfter = quantityBefore + request.getQuantity();

        // Update inventory item
        item.setQuantityInStock(quantityAfter);
        item.setLastRestockedAt(LocalDateTime.now());
        item = inventoryItemRepo.save(item);

        // Create transaction record
        InventoryTransaction transaction = InventoryTransaction.builder()
                .garageInventoryItem(item)
                .transactionType(TransactionType.STOCK_IN)
                .quantityChange(request.getQuantity())
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .unitPrice(request.getUnitPrice())
                .performedBy(technician)
                .supplierName(request.getSupplierName())
                .receiptNumber(request.getReceiptNumber())
                .notes(request.getNotes())
                .build();

        transactionRepo.save(transaction);
        log.info("Stock added. Before: {}, After: {}", quantityBefore, quantityAfter);

        // Check and resolve alerts if applicable
        checkAndResolveAlerts(item, technician);

        return mapper.entityToResponse(item);
    }

    /**
     * Remove stock from inventory (for job card usage, sales, etc.)
     */
    @Transactional
    public GarageInventoryItemResponse removeStock(RemoveStockRequest request) {
        log.info("Removing stock for inventory item: {}", request.getInventoryItemId());

        // Validate request
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        GarageInventoryItem item = inventoryItemRepo.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND));

        int quantityBefore = item.getQuantityInStock();

        // Validate sufficient stock
        if (quantityBefore < request.getQuantity()) {
            throw new IllegalStateException(
                    String.format("Insufficient stock. Available: %d, Requested: %d",
                            quantityBefore, request.getQuantity()));
        }

        int quantityAfter = quantityBefore - request.getQuantity();

        // Update inventory item
        item.setQuantityInStock(quantityAfter);
        item = inventoryItemRepo.save(item);

        // Create transaction record
        InventoryTransaction transaction = InventoryTransaction.builder()
                .garageInventoryItem(item)
                .transactionType(TransactionType.STOCK_OUT)
                .quantityChange(-request.getQuantity())
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .unitPrice(item.getUnitPrice())
                .performedBy(technician)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .build();

        transactionRepo.save(transaction);
        log.info("Stock removed. Before: {}, After: {}", quantityBefore, quantityAfter);

        // Check if alert should be created
        checkAndCreateAlert(item);

        return mapper.entityToResponse(item);
    }

    /**
     * Adjust stock (for corrections, damage, etc.)
     */
    @Transactional
    public GarageInventoryItemResponse adjustStock(AdjustStockRequest request) {
        log.info("Adjusting stock for inventory item: {}", request.getInventoryItemId());

        // Validate request
        if (request.getNewQuantity() == null || request.getNewQuantity() < 0) {
            throw new IllegalArgumentException("New quantity must be greater than or equal to 0");
        }
        if (request.getAdjustmentReason() == null || request.getAdjustmentReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Adjustment reason is required");
        }

        GarageInventoryItem item = inventoryItemRepo.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND));

        int quantityBefore = item.getQuantityInStock();
        int quantityAfter = request.getNewQuantity();
        int quantityChange = quantityAfter - quantityBefore;

        // Determine transaction type
        TransactionType transactionType;
        if (request.getAdjustmentReason().equals("DAMAGE")) {
            transactionType = TransactionType.DAMAGE;
        } else if (quantityChange > 0) {
            transactionType = TransactionType.ADJUSTMENT_IN;
        } else {
            transactionType = TransactionType.ADJUSTMENT_OUT;
        }

        // Update inventory item
        item.setQuantityInStock(quantityAfter);
        item = inventoryItemRepo.save(item);

        // Create transaction record
        InventoryTransaction transaction = InventoryTransaction.builder()
                .garageInventoryItem(item)
                .transactionType(transactionType)
                .quantityChange(quantityChange)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .unitPrice(item.getUnitPrice())
                .performedBy(technician)
                .reason(request.getAdjustmentReason())
                .notes(request.getNotes())
                .build();

        transactionRepo.save(transaction);
        log.info("Stock adjusted. Before: {}, After: {}, Change: {}",
                quantityBefore, quantityAfter, quantityChange);

        // Check alerts
        if (quantityChange > 0) {
            checkAndResolveAlerts(item, technician);
        } else {
            checkAndCreateAlert(item);
        }

        return mapper.entityToResponse(item);
    }

    /**
     * Get all inventory items for a garage
     */
    public List<GarageInventoryItemResponse> getGarageInventory(Integer garageId) {
        log.info("Fetching inventory for garage: {}", garageId);

        List<GarageInventoryItem> items = inventoryItemRepo
                .findByGarageIdAndIsActiveTrue(garageId);

        return items.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get low stock items
     */
    public List<GarageInventoryItemResponse> getLowStockItems(Integer garageId) {
        log.info("Fetching low stock items for garage: {}", garageId);

        List<GarageInventoryItem> items = inventoryItemRepo
                .findLowStockItemsByGarageId(garageId);

        return items.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get out of stock items
     */
    public List<GarageInventoryItemResponse> getOutOfStockItems(Integer garageId) {
        log.info("Fetching out of stock items for garage: {}", garageId);

        List<GarageInventoryItem> items = inventoryItemRepo
                .findOutOfStockItemsByGarageId(garageId);

        return items.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Search inventory items
     */
    public List<GarageInventoryItemResponse> searchInventoryItems(Integer garageId, String searchTerm) {
        log.info("Searching inventory for garage: {} with term: {}", garageId, searchTerm);

        List<GarageInventoryItem> items = inventoryItemRepo
                .searchInventoryItems(garageId, searchTerm);

        return items.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get transaction history for an item
     */
    public List<InventoryTransactionResponse> getTransactionHistory(Integer itemId) {
        log.info("Fetching transaction history for item: {}", itemId);

        List<InventoryTransaction> transactions = transactionRepo
                .findByGarageInventoryItemIdOrderByTransactionDateDesc(itemId);

        return transactions.stream()
                .map(mapper::transactionToResponse)
                .toList();
    }

    /**
     * Get inventory summary for a garage
     */
    public InventorySummaryResponse getInventorySummary(Integer garageId) {
        log.info("Fetching inventory summary for garage: {}", garageId);

        Long totalItems = inventoryItemRepo.countByGarageIdAndIsActiveTrue(garageId);
        Long lowStockItems = (long) inventoryItemRepo.findLowStockItemsByGarageId(garageId).size();
        Long outOfStockItems = (long) inventoryItemRepo.findOutOfStockItemsByGarageId(garageId).size();
        Long overstockedItems = (long) inventoryItemRepo.findOverstockedItemsByGarageId(garageId).size();
        Double totalValue = inventoryItemRepo.calculateTotalInventoryValue(garageId);
        Long activeAlerts = alertRepo.countActiveAlertsByGarageId(garageId);

        return InventorySummaryResponse.builder()
                .garageId(garageId)
                .totalItems(totalItems)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .overstockedItems(overstockedItems)
                .totalInventoryValue(totalValue != null ? totalValue : 0.0)
                .activeAlertsCount(activeAlerts)
                .build();
    }

    /**
     * Deactivate inventory item (soft delete)
     */
    @Transactional
    public void deactivateInventoryItem(Integer itemId) {
        log.info("Deactivating inventory item: {}", itemId);

        GarageInventoryItem item = inventoryItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(INVENTORY_ITEM_NOT_FOUND));

        item.setIsActive(false);
        inventoryItemRepo.save(item);

        log.info("Inventory item deactivated: {}", itemId);
    }

    /**
     * Check and create alert if needed
     */
    private void checkAndCreateAlert(GarageInventoryItem item) {
        AlertType alertType = null;

        if (item.getQuantityInStock() == 0) {
            alertType = AlertType.OUT_OF_STOCK;
        } else if (item.isLowStock()) {
            alertType = AlertType.LOW_STOCK;
        }

        if (alertType != null) {
            // Check if alert already exists
            boolean alertExists = alertRepo.findByGarageInventoryItemIdAndAlertTypeAndAlertStatus(
                    item.getId(), alertType, AlertStatus.ACTIVE).isPresent();

            if (!alertExists) {
                InventoryAlert alert = InventoryAlert.builder()
                        .garageInventoryItem(item)
                        .alertType(alertType)
                        .currentQuantity(item.getQuantityInStock())
                        .thresholdLevel(item.getMinimumStockLevel())
                        .suggestedOrderQuantity(item.getReorderQuantity())
                        .build();

                alertRepo.save(alert);
                log.info("Created {} alert for item: {}", alertType, item.getId());
            }
        }
    }

    /**
     * Check and resolve alerts if stock is back to normal
     */
    private void checkAndResolveAlerts(GarageInventoryItem item, Technician technician) {
        if (!item.isLowStock()) {
            List<InventoryAlert> activeAlerts = alertRepo
                    .findByGarageInventoryItemIdOrderByCreatedAtDesc(item.getId())
                    .stream()
                    .filter(alert -> alert.getAlertStatus() == AlertStatus.ACTIVE)
                    .toList();

            for (InventoryAlert alert : activeAlerts) {
                alert.resolve(technician, "Stock replenished automatically");
                alertRepo.save(alert);
                log.info("Auto-resolved alert: {}", alert.getId());
            }
        }
    }

    /**
     * Search parts in Part API by part number
     */
    public ProductDetailResponse searchPartByPartNumber(String partNumber) {
        log.info("Searching Part API for part number: {}", partNumber);

        try {
            ProductResponse response = partClient.getProductByPartNumber(partNumber);

            if (response != null && "success".equals(response.getStatus())
                    && response.getBody() != null) {
                log.info("Found part in Part API: {}", response.getBody().getName());
                return response.getBody();
            } else {
                log.warn("Part not found in Part API for part number: {}", partNumber);
                throw new ResourceNotFoundException("Part not found with part number: " + partNumber);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Part API for part number: {}. Error: {}", partNumber, e.getMessage());
            throw new ResourceNotFoundException("Failed to search Part API: " + e.getMessage());
        }
    }
}
