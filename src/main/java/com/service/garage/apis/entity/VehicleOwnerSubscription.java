package com.service.garage.apis.entity;

import java.time.LocalDate;

import com.service.garage.apis.util.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwnerSubscription {
	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_owner_id")
	private VehicleOwner vehicleOwner;

	@ManyToOne
	@JoinColumn(name = "garage_subscription_id")
	private GarageSubscription garageSubscription;

	private LocalDate purchaseDate;
	private LocalDate startDate;
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus status;

	@Column(columnDefinition = "TEXT")
	private String usageCounts; // JSON for tracking service usage
}
