package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.PartRequestStatus;
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

    @PrePersist
    protected void onCreate() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
        if (status == null) {
            status = PartRequestStatus.PENDING_CUSTOMER_RESPONSE;
        }
    }
}
