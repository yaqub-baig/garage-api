package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class VehicleListing {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
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

	@OneToOne(mappedBy = "vehicleListing", cascade = CascadeType.ALL)
	private VehicleSale sale;
}
