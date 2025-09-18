package com.autoadda.apis.garage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer claimId;

    private String documentType; // e.g., POLICY, IMAGE, BILL
    private String fileUrl; // stored in cloud / local storage
    private String fileName;
}
