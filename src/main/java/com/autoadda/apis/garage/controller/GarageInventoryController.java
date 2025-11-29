package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.*;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.service.GarageInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garage-inventory")
@RequiredArgsConstructor
@Slf4j
public class GarageInventoryController {

    private final GarageInventoryService inventoryService;

    /**
     * Add a new inventory item to garage
     * POST /garage-inventory
     */
    @PostMapping
    public ResponseEntity<GenericResponse> addInventoryItem(
            @RequestBody AddInventoryItemRequest request) {
        log.info("Adding inventory item for garage: {}", request.getGarageId());
        GarageInventoryItemResponse response = inventoryService.addInventoryItem(request);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory item added successfully", response));
    }

    /**
     * Get all inventory items for a garage
     * GET /garage-inventory/garage/{garageId}
     */
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getGarageInventory(
            @PathVariable Integer garageId) {
        log.info("Fetching inventory for garage: {}", garageId);
        List<GarageInventoryItemResponse> responses = inventoryService.getGarageInventory(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory retrieved successfully", responses));
    }

    /**
     * Get a specific inventory item by ID
     * GET /garage-inventory/{itemId}
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<GenericResponse> getInventoryItem(
            @PathVariable Integer itemId) {
        log.info("Fetching inventory item: {}", itemId);
        GarageInventoryItemResponse response = inventoryService.getInventoryItem(itemId);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory item retrieved successfully", response));
    }

    /**
     * Update inventory item details (thresholds, location, etc.)
     * PUT /garage-inventory/{itemId}
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<GenericResponse> updateInventoryItem(
            @PathVariable Integer itemId,
            @RequestBody UpdateInventoryItemRequest request) {
        log.info("Updating inventory item: {}", itemId);
        GarageInventoryItemResponse response = inventoryService.updateInventoryItem(itemId, request);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory item updated successfully", response));
    }

    /**
     * Add stock to an inventory item
     * POST /garage-inventory/{itemId}/add-stock
     */
    @PostMapping("/{itemId}/add-stock")
    public ResponseEntity<GenericResponse> addStock(
            @PathVariable Integer itemId,
            @RequestBody AddStockRequest request) {
        log.info("Adding stock to item: {}", itemId);
        request.setInventoryItemId(itemId);
        GarageInventoryItemResponse response = inventoryService.addStock(request);
        return ResponseEntity.ok(GenericResponse.success(
                "Stock added successfully", response));
    }

    /**
     * Remove stock from an inventory item
     * POST /garage-inventory/{itemId}/remove-stock
     */
    @PostMapping("/{itemId}/remove-stock")
    public ResponseEntity<GenericResponse> removeStock(
            @PathVariable Integer itemId,
            @RequestBody RemoveStockRequest request) {
        log.info("Removing stock from item: {}", itemId);
        request.setInventoryItemId(itemId);
        GarageInventoryItemResponse response = inventoryService.removeStock(request);
        return ResponseEntity.ok(GenericResponse.success(
                "Stock removed successfully", response));
    }

    /**
     * Adjust stock (for corrections, damage, etc.)
     * POST /garage-inventory/{itemId}/adjust-stock
     */
    @PostMapping("/{itemId}/adjust-stock")
    public ResponseEntity<GenericResponse> adjustStock(
            @PathVariable Integer itemId,
            @RequestBody AdjustStockRequest request) {
        log.info("Adjusting stock for item: {}", itemId);
        request.setInventoryItemId(itemId);
        GarageInventoryItemResponse response = inventoryService.adjustStock(request);
        return ResponseEntity.ok(GenericResponse.success(
                "Stock adjusted successfully", response));
    }

    /**
     * Get low stock items for a garage
     * GET /garage-inventory/garage/{garageId}/low-stock
     */
    @GetMapping("/garage/{garageId}/low-stock")
    public ResponseEntity<GenericResponse> getLowStockItems(
            @PathVariable Integer garageId) {
        log.info("Fetching low stock items for garage: {}", garageId);
        List<GarageInventoryItemResponse> responses = inventoryService.getLowStockItems(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Low stock items retrieved successfully", responses));
    }

    /**
     * Get out of stock items
     * GET /garage-inventory/garage/{garageId}/out-of-stock
     */
    @GetMapping("/garage/{garageId}/out-of-stock")
    public ResponseEntity<GenericResponse> getOutOfStockItems(
            @PathVariable Integer garageId) {
        log.info("Fetching out of stock items for garage: {}", garageId);
        List<GarageInventoryItemResponse> responses = inventoryService.getOutOfStockItems(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Out of stock items retrieved successfully", responses));
    }

    /**
     * Search inventory items
     * GET /garage-inventory/garage/{garageId}/search?q={searchTerm}
     */
    @GetMapping("/garage/{garageId}/search")
    public ResponseEntity<GenericResponse> searchInventoryItems(
            @PathVariable Integer garageId,
            @RequestParam String q) {
        log.info("Searching inventory for garage: {} with term: {}", garageId, q);
        List<GarageInventoryItemResponse> responses =
                inventoryService.searchInventoryItems(garageId, q);
        return ResponseEntity.ok(GenericResponse.success(
                "Search completed successfully", responses));
    }

    /**
     * Get transaction history for an item
     * GET /garage-inventory/{itemId}/transactions
     */
    @GetMapping("/{itemId}/transactions")
    public ResponseEntity<GenericResponse> getTransactionHistory(
            @PathVariable Integer itemId) {
        log.info("Fetching transaction history for item: {}", itemId);
        List<InventoryTransactionResponse> responses =
                inventoryService.getTransactionHistory(itemId);
        return ResponseEntity.ok(GenericResponse.success(
                "Transaction history retrieved successfully", responses));
    }

    /**
     * Get inventory summary for a garage
     * GET /garage-inventory/garage/{garageId}/summary
     */
    @GetMapping("/garage/{garageId}/summary")
    public ResponseEntity<GenericResponse> getInventorySummary(
            @PathVariable Integer garageId) {
        log.info("Fetching inventory summary for garage: {}", garageId);
        InventorySummaryResponse response = inventoryService.getInventorySummary(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory summary retrieved successfully", response));
    }

    /**
     * Search for a part by part number in Part API
     * GET /garage-inventory/search-part?partNumber={partNumber}
     */
    @GetMapping("/search-part")
    public ResponseEntity<GenericResponse> searchPartByPartNumber(
            @RequestParam String partNumber) {
        log.info("Searching for part with part number: {}", partNumber);
        ProductDetailResponse product = inventoryService.searchPartByPartNumber(partNumber);
        return ResponseEntity.ok(GenericResponse.success(
                "Part found successfully", product));
    }

    /**
     * Deactivate inventory item (soft delete)
     * DELETE /garage-inventory/{itemId}
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<GenericResponse> deactivateInventoryItem(
            @PathVariable Integer itemId) {
        log.info("Deactivating inventory item: {}", itemId);
        inventoryService.deactivateInventoryItem(itemId);
        return ResponseEntity.ok(GenericResponse.success(
                "Inventory item deactivated successfully", null));
    }
}
