package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.CertificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "technician_certification", indexes = @Index(name = "idx_tech_certification_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianCertification extends TechnicianProfileItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "certification_type", length = 30)
    private CertificationType certificationType;

    @Column(name = "name")
    private String name;

    @Column(name = "issuing_organization")
    private String issuingOrganization;

    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToOne
    @JoinColumn(name = "document_file_id")
    private MediaFile document;
}
