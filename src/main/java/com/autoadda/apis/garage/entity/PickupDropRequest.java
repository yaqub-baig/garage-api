package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.RequestType;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupDropRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer appointmentId;
    private Integer vehicleOwnerId;
    private Integer garageId;

    @Enumerated(EnumType.STRING)
    private RequestType requestType; // PICKUP, DROPOFF, BOTH

    private String pickupAddress;
    private String dropOffAddress;

    @Enumerated(EnumType.STRING)
    private Status status; // REQUESTED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDateTime scheduledTime;

    private Integer assignedDriverId; // Optional

    private String notes;
}
