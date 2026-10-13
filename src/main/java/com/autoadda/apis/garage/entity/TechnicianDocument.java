package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.TechnicianDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "technician_document", indexes = @Index(name = "idx_tech_document_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianDocument extends TechnicianProfileItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 30)
    private TechnicianDocumentType documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToOne
    @JoinColumn(name = "file_id")
    private MediaFile file;
}
