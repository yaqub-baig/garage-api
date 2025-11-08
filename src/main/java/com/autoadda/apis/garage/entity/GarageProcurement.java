package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.PartCondition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageProcurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "part_request_item_id", nullable = false)
    private PartRequestItem partRequestItem;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "supplier_contact")
    private String supplierContact;

    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;


    @Column(name = "part_condition", nullable = false)
    @Enumerated(EnumType.STRING)
    private PartCondition partCondition;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "procurement_date", nullable = false)
    private LocalDateTime procurementDate;

    @ManyToOne
    @JoinColumn(name = "procured_by_user_id")
    private Technician procuredBy;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (procurementDate == null) {
            procurementDate = LocalDateTime.now();
        }
    }
}
