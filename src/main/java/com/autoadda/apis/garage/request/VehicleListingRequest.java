package com.autoadda.apis.garage.request;

import java.time.LocalDate;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.VehicleSale;
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
public class VehicleListingRequest {
	private Integer id;
	private Garage garage;
	private String vin;
	private String make;
	private String model;
	private int year;
	private double price;
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status; // LISTED, SOLD, REMOVED

	private LocalDate listedDate;

	private VehicleSaleRequest sale; // nullable until sold
}
