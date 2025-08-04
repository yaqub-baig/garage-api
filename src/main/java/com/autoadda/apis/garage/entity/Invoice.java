package com.autoadda.apis.garage.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

	private int loyaltyPointsUsed;
	private int loyaltyPointsEarned;

	@Enumerated(EnumType.STRING)
	private Status paymentStatus;

	private String paymentMethod; // e.g., "CASH", "CREDIT_CARD", "ONLINE"

	private Boolean isPaid;

	private String notes;

	private LocalDateTime createdAt;
}
