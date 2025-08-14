package com.autoadda.apis.garage.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "garage_id", referencedColumnName = "id")
	private Garage garage;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "vehicle_id", referencedColumnName = "id")
	private VehicleDetails vehicle;

}
