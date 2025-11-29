package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.ResolveAlertRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.InventoryAlertResponse;
import com.autoadda.apis.garage.service.InventoryAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory-alerts")
@RequiredArgsConstructor
@Slf4j
public class InventoryAlertController {

    private final InventoryAlertService alertService;

    /**
     * Get all active alerts for a garage
     * GET /inventory-alerts/garage/{garageId}/active
     */
    @GetMapping("/garage/{garageId}/active")
    public ResponseEntity<GenericResponse> getActiveAlerts(
            @PathVariable Integer garageId) {
        log.info("Fetching active alerts for garage: {}", garageId);
        List<InventoryAlertResponse> responses = alertService.getActiveAlerts(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Active alerts retrieved successfully", responses));
    }

    /**
     * Get alert count for a garage
     * GET /inventory-alerts/garage/{garageId}/count
     */
    @GetMapping("/garage/{garageId}/count")
    public ResponseEntity<GenericResponse> getAlertCount(
            @PathVariable Integer garageId) {
        log.info("Fetching alert count for garage: {}", garageId);
        Long count = alertService.getActiveAlertCount(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Alert count retrieved successfully", count));
    }

    /**
     * Resolve an alert
     * PUT /inventory-alerts/{alertId}/resolve
     */
    @PutMapping("/{alertId}/resolve")
    public ResponseEntity<GenericResponse> resolveAlert(
            @PathVariable Integer alertId,
            @RequestBody ResolveAlertRequest request) {
        log.info("Resolving alert: {}", alertId);
        InventoryAlertResponse response = alertService.resolveAlert(alertId, request);
        return ResponseEntity.ok(GenericResponse.success(
                "Alert resolved successfully", response));
    }

    /**
     * Dismiss an alert
     * PUT /inventory-alerts/{alertId}/dismiss
     */
    @PutMapping("/{alertId}/dismiss")
    public ResponseEntity<GenericResponse> dismissAlert(
            @PathVariable Integer alertId,
            @RequestBody ResolveAlertRequest request) {
        log.info("Dismissing alert: {}", alertId);
        InventoryAlertResponse response = alertService.dismissAlert(alertId, request);
        return ResponseEntity.ok(GenericResponse.success(
                "Alert dismissed successfully", response));
    }

    /**
     * Get alert history for an inventory item
     * GET /inventory-alerts/item/{itemId}/history
     */
    @GetMapping("/item/{itemId}/history")
    public ResponseEntity<GenericResponse> getAlertHistory(
            @PathVariable Integer itemId) {
        log.info("Fetching alert history for item: {}", itemId);
        List<InventoryAlertResponse> responses = alertService.getAlertHistory(itemId);
        return ResponseEntity.ok(GenericResponse.success(
                "Alert history retrieved successfully", responses));
    }
}
