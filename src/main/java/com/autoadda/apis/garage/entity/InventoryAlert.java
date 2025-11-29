package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.AlertStatus;
import com.autoadda.apis.garage.util.AlertType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "garage_inventory_item_id", nullable = false)
    private GarageInventoryItem garageInventoryItem;

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "alert_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AlertStatus alertStatus = AlertStatus.ACTIVE;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "threshold_level")
    private Integer thresholdLevel;

    @Column(name = "suggested_order_quantity")
    private Integer suggestedOrderQuantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "resolved_by_technician_id")
    private Technician resolvedBy;

    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (alertStatus == null) {
            alertStatus = AlertStatus.ACTIVE;
        }
    }

    public void resolve(Technician technician, String notes) {
        this.alertStatus = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = technician;
        this.resolutionNotes = notes;
    }

    public void dismiss(Technician technician, String notes) {
        this.alertStatus = AlertStatus.DISMISSED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = technician;
        this.resolutionNotes = notes;
    }
}
