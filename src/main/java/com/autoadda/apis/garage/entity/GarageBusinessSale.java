package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "garage_business_sale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBusinessSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private GarageBusinessListing listing;

    @Column(name = "buyer_id", nullable = false)
    private Integer buyerId;

    @Column(name = "sale_price", nullable = false)
    private Double salePrice;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL)
    private GarageBusinessSaleInvoice invoice;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (saleDate == null) {
            saleDate = LocalDate.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
