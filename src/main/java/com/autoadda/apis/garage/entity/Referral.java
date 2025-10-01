package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.ReferredEntityType;
import com.autoadda.apis.garage.util.ReferrerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String referralCode;

    @Enumerated(EnumType.STRING)
    private ReferrerType referrerType; // VEHICLE_OWNER, GARAGE

    private Integer referrerId; // VehicleOwner.id or Garage.id

    private Integer referredEntityId; // new customer or garage

    @Enumerated(EnumType.STRING)
    private ReferredEntityType referredEntityType; // VEHICLE_OWNER, GARAGE

    private boolean rewardIssued;

    private LocalDateTime createdAt;
}
