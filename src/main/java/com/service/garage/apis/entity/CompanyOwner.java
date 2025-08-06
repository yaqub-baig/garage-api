package com.service.garage.apis.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyOwner extends VehicleOwner {

	private String companyName;
	private String tradeLicenseNumber;

	private String contactPerson;
}
