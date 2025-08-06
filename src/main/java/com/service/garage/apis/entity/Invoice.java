package com.service.garage.apis.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.service.garage.apis.util.Status;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
	private List<InvoiceLineItem> lineItems = new ArrayList<>();

	private Double totalAmount;

	private Double taxRate; // e.g., 10.0 means 10%
	private Double taxAmount;
	private Double amountPaid;

	private int loyaltyPointsUsed;
	private int loyaltyPointsEarned;

	@Enumerated(EnumType.STRING)
	private Status paymentStatus;

	@ManyToOne
	@JoinColumn(name = "job_card_id")
	private JobCard jobCard;

	private String paymentMethod; // e.g., "CASH", "CREDIT_CARD", "ONLINE"

	private Boolean isPaid;

	private String notes;

	private LocalDateTime createdAt;
}
