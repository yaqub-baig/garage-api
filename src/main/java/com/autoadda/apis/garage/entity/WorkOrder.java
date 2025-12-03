package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Garage garage;

    @OneToOne
    private AppointmentDetails appointment;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<JobCard> jobCards = new ArrayList<> ( );

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    @Builder.Default
    private List<VehicleDelivery> vehicleDeliveries = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime deliveryDate;
    private String deliveryRemarks;

    private Boolean customerAcknowledged;

    private Double customerNegotiatedAmount;
    private String customerNegotiatedComment;

    private Double garageNegotiatedAmount;
    private String garageNegotiatedComment;

    private LocalDateTime workStartDate;
    private LocalDateTime workCompletedDate;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "work_order_status_timestamps",
            joinColumns = @JoinColumn(name = "work_order_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "timestamp")
    @Builder.Default
    private Map<WorkOrderStatus, LocalDateTime> statusTimestamps = new EnumMap<> (WorkOrderStatus.class);

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now ( );
        status = WorkOrderStatus.CREATED;
        if ( jobCards == null ) {
            jobCards = new ArrayList<> ( );
        }
        if ( statusTimestamps == null ) {
            statusTimestamps = new EnumMap<>(WorkOrderStatus.class);
        }
        // Record the initial CREATED status timestamp
        statusTimestamps.put ( WorkOrderStatus.CREATED, LocalDateTime.now ( ) );
    }

    public void updateStatus(WorkOrderStatus newStatus) {
        if ( this.status != newStatus ) {
            this.status = newStatus;
            this.statusTimestamps.put ( newStatus, LocalDateTime.now ( ) );
        }

    }

    /**
     * Get the most recent active delivery (non-terminal status)
     * @return VehicleDelivery or null if no active delivery exists
     */
    public VehicleDelivery getActiveDelivery() {
        if (vehicleDeliveries == null || vehicleDeliveries.isEmpty()) {
            return null;
        }

        return vehicleDeliveries.stream()
                .filter(delivery -> delivery.getStatus() != null)
                .filter(delivery ->
                    delivery.getStatus() != com.autoadda.apis.garage.util.DeliveryStatus.APPROVED &&
                    delivery.getStatus() != com.autoadda.apis.garage.util.DeliveryStatus.REJECTED
                )
                .max((d1, d2) -> d1.getCreatedDate().compareTo(d2.getCreatedDate()))
                .orElse(null);
    }

    /**
     * Check if a new delivery can be initiated
     * @return true if no active delivery exists, false otherwise
     */
    public boolean canInitiateDelivery() {
        return getActiveDelivery() == null;
    }

    /**
     * Get the current delivery status for frontend display
     * @return DeliveryStatus or null if no active delivery
     */
    public com.autoadda.apis.garage.util.DeliveryStatus getCurrentDeliveryStatus() {
        VehicleDelivery activeDelivery = getActiveDelivery();
        return activeDelivery != null ? activeDelivery.getStatus() : null;
    }


}
