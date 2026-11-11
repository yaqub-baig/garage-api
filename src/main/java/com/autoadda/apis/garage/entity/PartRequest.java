package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.PartRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "job_card_id", nullable = false)
    private JobCard jobCard;

    @ManyToOne
    @JoinColumn(name = "requested_by_technician_id", nullable = false)
    private Technician requestedBy;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PartRequestStatus status;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "customer_response_date")
    private LocalDateTime customerResponseDate;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @OneToMany(mappedBy = "partRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PartRequestItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "partRequest", cascade = CascadeType.ALL)
    private CustomerPartResponse customerResponse;

    @Column(name = "notes")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "voice_note_id")
    private MediaFile voiceNote;

    /** When each status was reached (for the parts timeline). Empty for requests created before this existed. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "part_request_status_timestamps", joinColumns = @JoinColumn(name = "part_request_id"))
    @MapKeyColumn(name = "status", length = 40)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "timestamp")
    @Builder.Default
    private Map<PartRequestStatus, LocalDateTime> statusTimestamps = new EnumMap<>(PartRequestStatus.class);

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null) {
            status = PartRequestStatus.PENDING_CUSTOMER_RESPONSE;
        }
        if (statusTimestamps == null) {
            statusTimestamps = new EnumMap<>(PartRequestStatus.class);
        }
        statusTimestamps.putIfAbsent(status, requestDate);
    }

    /** Changes the status and records when it happened (no-op when the status is unchanged). */
    public void updateStatus(PartRequestStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            if (statusTimestamps == null) {
                statusTimestamps = new EnumMap<>(PartRequestStatus.class);
            }
            statusTimestamps.put(newStatus, LocalDateTime.now());
        }
    }
}
