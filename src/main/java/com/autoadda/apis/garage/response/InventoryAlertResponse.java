package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.AlertStatus;
import com.autoadda.apis.garage.util.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertResponse {
    private Integer id;
    private Integer inventoryItemId;
    private String partName;
    private String partNumber;
    private AlertType alertType;
    private AlertStatus alertStatus;
    private Integer currentQuantity;
    private Integer thresholdLevel;
    private Integer suggestedOrderQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer resolvedByTechnicianId;
    private String resolvedByTechnicianName;
    private String resolutionNotes;
}
