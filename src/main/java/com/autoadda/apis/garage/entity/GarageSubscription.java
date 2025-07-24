package com.autoadda.apis.garage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageSubscription {
	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "garage_id")
	private Garage garage;

	private String name;
	private String description;
	private Double price;
	private int durationMonths;

	@Column(columnDefinition = "TEXT")
	private String servicesIncluded; // JSON string or convert to @ElementCollection

	private Boolean isActive;
}
