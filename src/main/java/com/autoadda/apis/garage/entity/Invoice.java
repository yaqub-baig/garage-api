package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.InvoiceStatus;
import com.autoadda.apis.garage.util.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<> ( );

    private Double totalAmount;
    private Double subtotal;

    private Double taxRate; // e.g., 10.0 means 10%
    private Double taxAmount;
    private Double subscriptionDiscount; // Discount applied from subscription
    private Double amountPaid;

    private int loyaltyPointsUsed;
    private int loyaltyPointsEarned;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "job_card_id")
    private JobCard jobCard;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // e.g., "CASH", "CREDIT_CARD", "ONLINE"

    private Boolean isPaid;

    private String notes;

    private LocalDateTime createdAt;
}
