package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<WorkOrderStatus, LocalDateTime> statusTimestamps = new HashMap<> ( );

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now ( );
        status = WorkOrderStatus.CREATED;
        if ( jobCards == null ) {
            jobCards = new ArrayList<> ( );
        }
        if ( statusTimestamps == null ) {
            statusTimestamps = new HashMap<> ( );
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


}
