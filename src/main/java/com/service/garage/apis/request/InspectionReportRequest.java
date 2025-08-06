package com.service.garage.apis.request;

import java.time.LocalDate;

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
public class InspectionReportRequest {

	private Integer id;

	private LocalDate inspectionDate;

	private String inspectorName;

	private String remarks;

	private Boolean passed;

	private VehicleSaleRequest vehicleSale;
}
