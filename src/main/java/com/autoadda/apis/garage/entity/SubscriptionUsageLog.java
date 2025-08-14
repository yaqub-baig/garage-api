package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class SubscriptionUsageLog {
	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne
	private VehicleOwnerSubscription subscription;

	private String serviceName;
	private LocalDate usedOn;

	@ManyToOne
	private Technician technician;

	@ManyToOne
	private JobCard jobCard;
}
