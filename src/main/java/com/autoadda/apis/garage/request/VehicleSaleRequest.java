package com.autoadda.apis.garage.request;

import java.time.LocalDate;

import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleRequest {

	private Integer id;

	private VehicleDetails vehicle;
	private Integer vehicleId;
	private VehicleOwner previousOwner;
	private Integer previousOwnerId;
	private VehicleOwner newOwner;
	private Integer newOwnerId;
	private LocalDate saleDate;

	private double salePrice;

	@Enumerated(EnumType.STRING)
	private Status status; // ACTIVE, REMOVED

	private InspectionReportRequest inspectionReport;

	private VehicleSaleInvoiceRequest invoice;
}
