package com.autoadda.apis.garage.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "role_code")
	private String roleCode;

	@Column(name = "description")
	private String description;
}
