package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "delivered_by_technician_id")
    private Technician deliveredBy;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_delivery_id")
    @Builder.Default
    private List<MediaFile> deliveryPhotos = new ArrayList<>();

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "customer_approved")
    private Boolean customerApproved;

    @Column(name = "customer_comments", length = 2000)
    private String customerComments;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING_VERIFICATION;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    // Re-verification of 12 items from VehicleReceived
    @Column(name = "is_main_key_returned")
    private Boolean isMainKeyReturned;

    @Column(name = "is_spare_key_returned")
    private Boolean isSpareKeyReturned;

    @Column(name = "is_tool_kit_returned")
    private Boolean isToolKitReturned;

    @Column(name = "is_jack_returned")
    private Boolean isJackReturned;

    @Column(name = "is_wheel_spanner_returned")
    private Boolean isWheelSpannerReturned;

    @Column(name = "is_spare_tyre_returned")
    private Boolean isSpareTyreReturned;

    @Column(name = "is_fire_extinguisher_returned")
    private Boolean isFireExtinguisherReturned;

    @Column(name = "is_first_aid_kit_returned")
    private Boolean isFirstAidKitReturned;

    @Column(name = "is_central_lock_remote_returned")
    private Boolean isCentralLockRemoteReturned;

    @Column(name = "is_rc_book_returned")
    private Boolean isRCBookReturned;

    @Column(name = "is_insurance_copy_returned")
    private Boolean isInsuranceCopyReturned;

    @Column(name = "is_puc_returned")
    private Boolean isPUCReturned;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deliveryDate == null) {
            deliveryDate = LocalDateTime.now();
        }
        if (status == null) {
            status = DeliveryStatus.PENDING_VERIFICATION;
        }
        if (customerApproved == null) {
            customerApproved = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return createdAt;
    }
}
