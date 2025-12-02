package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.JobCardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCard {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "service_start_time")
    private LocalTime serviceStartTime;

    @Column(name = "service_end_time")
    private LocalTime serviceEndTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobCardStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "garage_id", referencedColumnName = "id")
    private Garage garage;

    @ManyToOne
    @JoinColumn(name = "technician_id", referencedColumnName = "id")
    private Technician technician;

    @ManyToOne
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private AppointmentDetails appointment;

    @ManyToOne
    @JoinColumn(name = "appointment_service_id", referencedColumnName = "id")
    private AppointmentServices service;

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "comments")
    private String comments;

    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime qualityCheckedAt;
    private String qualityComments;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_card_status_timestamps",
            joinColumns = @JoinColumn(name = "job_card_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "timestamp")
    @Builder.Default
    private Map<JobCardStatus, LocalDateTime> statusTimestamps = new EnumMap<>(JobCardStatus.class );

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now ( );
        status = JobCardStatus.CREATED;
        if ( statusTimestamps == null ) {
            statusTimestamps = new EnumMap<> (JobCardStatus.class );
        }
        // Record the initial CREATED status timestamp
        statusTimestamps.put ( JobCardStatus.CREATED, LocalDateTime.now ( ) );
    }

    public void updateStatus(JobCardStatus newStatus) {
        if ( this.status != newStatus ) {
            this.status = newStatus;
            this.statusTimestamps.put ( newStatus, LocalDateTime.now ( ) );
        }
    }

}
