package com.autoadda.apis.garage.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetails {

	private Integer id;

	private String vin;

	private String year;

	private String name;

	private String odometer;

	private String color;

	private String engineSize;

	private String noOfDoors;

	private String noOfSeats;

	private String noOfCylinders;

	private String noOfSpeeds;

	private String fuelType;

	private Integer currentMileage;

	private Integer lastServiceMileage;

	private LocalDate lastServiceDate;

	private String make;

	private String model;

	private String preferredGarageId;

	private Integer customerId;

//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "insurance_id", referencedColumnName = "id")
//	private InsuranceDetails insurance;

	private VehicleRegistration registration;

}
