package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwnerSubscription {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer customerId;
    @ManyToOne
    @JoinColumn(name = "garage_subscription_id")
    private GarageSubscription garageSubscription;
    private LocalDate purchaseDate;
    private LocalDate startDate;
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    @Column(columnDefinition = "TEXT")
    private String usageCounts; // JSON for tracking service usage
}
