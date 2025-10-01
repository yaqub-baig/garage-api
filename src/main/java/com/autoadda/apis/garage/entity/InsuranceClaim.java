package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer vehicleOwnerId;
    private Integer garageId;
    private Integer jobCardId; // Optional: link to a repair job
    private String policyNumber;
    private String insurerName;
    private String vehicleNumber;

    private String claimDescription;
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String approvalRemarks;

    private BigDecimal estimatedAmount;
    private BigDecimal approvedAmount;

    private LocalDateTime updatedAt;
}
