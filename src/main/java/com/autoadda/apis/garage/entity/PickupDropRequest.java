package com.autoadda.apis.garage.entity;

import java.time.LocalDateTime;

import com.autoadda.apis.garage.util.RequestType;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupDropRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer appointmentId;
	private Integer vehicleOwnerId;
	private Integer garageId;

	@Enumerated(EnumType.STRING)
	private RequestType requestType; // PICKUP, DROPOFF, BOTH

	private String pickupAddress;
	private String dropOffAddress;

	@Enumerated(EnumType.STRING)
	private Status status; // REQUESTED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

	private LocalDateTime scheduledTime;

	private Integer assignedDriverId; // Optional

	private String notes;
}
