package com.service.garage.apis.entity;

import com.service.garage.apis.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class VehicleRegistration {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "state")
	private Status state;

	@Column(name = "code")
	private String code;

	@Column(name = "category")
	private String category;

	@Column(name = "plate_number")
	private String plateNumber;

	@Column(name = "registered_name")
	private String registeredName;
}
