package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FutureAppointment {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private Integer vehicleId;
	private String garageId;
	private String serviceType;

	private LocalDate scheduledDate;
	private Integer estimatedMileage;

	@Enumerated(EnumType.STRING)
	private Status status; // SCHEDULED, CONFIRMED, CANCELLED

	private String createdBy; // SYSTEM or USER
}