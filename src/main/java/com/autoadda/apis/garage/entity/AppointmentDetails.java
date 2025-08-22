package com.autoadda.apis.garage.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.*;
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
public class AppointmentDetails {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "appointment_reference_id")
	private String appointmentReferenceId;

	@Column(name = "schedule_date")
	private LocalDate scheduleDate;

	@Column(name = "service_start_time")
	private LocalTime serviceStartTime;

	@Column(name = "service_end_time")
	private LocalTime serviceEndTime;

	@Column(name = "delivery_time")
	private LocalTime deliveryTime;

	@Column(name = "status")
	private Status status;

	@Column(name = "comments")
	private String comments;

	@Column(name = "insert_tms")
	private LocalDate insert_tms;

	@ManyToOne
	@JoinColumn(name = "garage_id", nullable = false)
	private Garage garage;

	@Column(name = "customer_id", nullable = false)
	private Integer customerId;

	@Column(name = "vehicle_id", nullable = true)
	private Integer vehicleId;

	@ManyToOne
	@JoinColumn(name = "appointment_slot_id", nullable = false)
	private AppointmentSlot slot;

	@Column(name = "technician_id", nullable = true)
	private Integer technicianId;
}
