package com.autoadda.apis.garage.entity;

import java.time.LocalTime;

import com.autoadda.apis.garage.util.Status;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer customerId; // Reference to the customer (could be VehicleOwner or User)

	private Boolean emailOnServiceUpdates;
	private Boolean smsOnServiceUpdates;

	private Boolean emailOnAppointments;
	private Boolean smsOnAppointments;

	private Boolean emailOnReminders;
	private Boolean smsOnReminders;

	private Boolean emailOnOffers;
	private Boolean smsOnOffers;

	private Boolean emailOnQuotationRequests;
	private Boolean smsOnQuotationRequests;

	@Enumerated(EnumType.STRING)
	private Status status;

	// Time range (optional): Do not disturb outside this window
	private LocalTime doNotDisturbStart; // e.g., 21:00 for 9 PM
	private LocalTime doNotDisturbEnd; // e.g., 07:00 for 7 AM
	// Getters and setters
}
