package com.service.garage.apis.entity;

import java.util.List;

import com.service.garage.apis.util.OwnerType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "owner_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String email;

	private String mobileNumber;

	private int loyaltyPoints = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", insertable = false, updatable = false)
	private OwnerType ownerType;

//	@OneToMany(mappedBy = "vehicleOwner")
//	private List<VehicleOwnerSubscription> subscriptions;

	@ManyToOne
	@JoinColumn(name = "location_id", referencedColumnName = "id")
	private Location location;

	@OneToMany(mappedBy = "vehicleOwner", cascade = CascadeType.ALL)
	private List<VehicleDetails> vehicleDetails;

	@ManyToOne
	@JoinColumn(name = "favorite_garage_id")
	private Garage favoriteGarage;
}
