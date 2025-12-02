package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.InventoryAlert;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageInventoryMapper;
import com.autoadda.apis.garage.repository.InventoryAlertRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.ResolveAlertRequest;
import com.autoadda.apis.garage.response.InventoryAlertResponse;
import com.autoadda.apis.garage.util.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAlertService {

    private final InventoryAlertRepository alertRepo;
    private final TechnicianRepository technicianRepo;
    private final GarageInventoryMapper mapper;

    /**
     * Get all active alerts for a garage
     */
    public List<InventoryAlertResponse> getActiveAlerts(Integer garageId) {
        log.info("Fetching active alerts for garage: {}", garageId);

        List<InventoryAlert> alerts = alertRepo.findActiveAlertsByGarageId(garageId);

        return alerts.stream()
                .map(mapper::alertToResponse)
                .toList();
    }

    /**
     * Get active alerts by type
     */
    public List<InventoryAlertResponse> getActiveAlertsByType(Integer garageId, AlertType alertType) {
        log.info("Fetching active {} alerts for garage: {}", alertType, garageId);

        List<InventoryAlert> alerts = alertRepo.findActiveAlertsByGarageIdAndType(garageId, alertType);

        return alerts.stream()
                .map(mapper::alertToResponse)
                .toList();
    }

    /**
     * Get count of active alerts for a garage
     */
    public Long getActiveAlertCount(Integer garageId) {
        log.info("Fetching active alert count for garage: {}", garageId);

        return alertRepo.countActiveAlertsByGarageId(garageId);
    }

    /**
     * Resolve an alert
     */
    @Transactional
    public InventoryAlertResponse resolveAlert(Integer alertId, ResolveAlertRequest request) {
        log.info("Resolving alert: {}", alertId);

        InventoryAlert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        alert.resolve(technician, request.getNotes());
        alert = alertRepo.save(alert);

        log.info("Alert resolved: {}", alertId);

        return mapper.alertToResponse(alert);
    }

    /**
     * Dismiss an alert
     */
    @Transactional
    public InventoryAlertResponse dismissAlert(Integer alertId, ResolveAlertRequest request) {
        log.info("Dismissing alert: {}", alertId);

        InventoryAlert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        Technician technician = technicianRepo.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        alert.dismiss(technician, request.getNotes());
        alert = alertRepo.save(alert);

        log.info("Alert dismissed: {}", alertId);

        return mapper.alertToResponse(alert);
    }

    /**
     * Get alert history for an inventory item
     */
    public List<InventoryAlertResponse> getAlertHistory(Integer itemId) {
        log.info("Fetching alert history for item: {}", itemId);

        List<InventoryAlert> alerts = alertRepo
                .findByGarageInventoryItemIdOrderByCreatedAtDesc(itemId);

        return alerts.stream()
                .map(mapper::alertToResponse)
                .toList();
    }
}
