package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "vin")
	private String vin;

	@Column(name = "year")
	private String year;

	@Column(name = "name")
	private String name;

	@Column(name = "odometer")
	private String odometer;

	@Column(name = "color")
	private String color;

	@Column(name = "engine_size")
	private String engineSize;

	@Column(name = "no_of_doors")
	private String noOfDoors;

	@Column(name = "no_of_seats")
	private String noOfSeats;

	@Column(name = "no_of_cylinders")
	private String noOfCylinders;

	@Column(name = "no_of_speeds")
	private String noOfSpeeds;

	@Column(name = "fuel_type")
	private String fuelType;

	@Column(name = "current_mileage")
	private Integer currentMileage;

	@Column(name = "last_service_mileage")
	private Integer lastServiceMileage;

	@Column(name = "last_service_date")
	private LocalDate lastServiceDate;

	@Column(name = "make")
	private String make;
	
	@Column(name = "model")
	private String model;
	
	@Column(name = "preferred_garage_id")
	private String preferredGarageId;
	
	@Column(name = "customer_id")
	private Integer customerId;

//	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name = "insurance_id", referencedColumnName = "id")
//	private InsuranceDetails insurance;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "registration_id", referencedColumnName = "id")
	private VehicleRegistration registration;

}
