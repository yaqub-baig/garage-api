package com.service.garage.apis.entity;

import java.time.LocalDateTime;

import com.service.garage.apis.util.ReferredEntityType;
import com.service.garage.apis.util.ReferrerType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Referral {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String referralCode;

	@Enumerated(EnumType.STRING)
	private ReferrerType referrerType; // VEHICLE_OWNER, GARAGE

	private Integer referrerId; // VehicleOwner.id or Garage.id

	private Integer referredEntityId; // new customer or garage

	@Enumerated(EnumType.STRING)
	private ReferredEntityType referredEntityType; // VEHICLE_OWNER, GARAGE

	private boolean rewardIssued;

	private LocalDateTime createdAt;
}
