package com.service.garage.apis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageSubscription {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "garage_id")
	private Garage garage;

	private String name;

	private String description;

	private Double price;

	private int durationMonths;

	private String brands;

	private String benefits;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinTable(
			name = "garage_subscription_services",
			joinColumns = @JoinColumn(name = "subscription_id"),
			inverseJoinColumns = @JoinColumn(name = "service_id")
	)
	private List<AvailableService> servicesIncluded = new ArrayList<>();

	private Boolean isActive;

	@PrePersist
	public void prePersist() {
		isActive = true;
	}
}
