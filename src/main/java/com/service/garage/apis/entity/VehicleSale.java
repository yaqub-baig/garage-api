package com.service.garage.apis.entity;

import java.time.LocalDate;

import com.service.garage.apis.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private VehicleDetails vehicle;

	@ManyToOne
	private VehicleOwner previousOwner;

	@ManyToOne
	private VehicleOwner newOwner;

	private LocalDate saleDate;

	private Double salePrice;

	@OneToOne
	private Customer buyer;

	private String paymentMode; // e.g., CASH, CREDIT, LOAN

	@Enumerated(EnumType.STRING)
	private Status status; // ACTIVE, REMOVED

	@OneToOne(mappedBy = "vehicleSale", cascade = CascadeType.ALL)
	private InspectionReport inspectionReport;

	@OneToOne(mappedBy = "vehicleSale", cascade = CascadeType.ALL)
	private VehicleSaleInvoice invoice;

	@OneToOne
	@JoinColumn(name = "vehicle_listing_id")
	private VehicleListing vehicleListing;
}
