package com.autoadda.apis.garage.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class JobCard {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(name = "service_start_time")
	private LocalTime serviceStartTime;

	@Column(name = "service_end_time")
	private LocalTime serviceEndTime;

	@Column(name = "status")
	private Status status;

	@Column(name = "created_at")
	private LocalDate createdAt;

	@ManyToOne
	private Garage garage;

	@ManyToOne
	private Technician technician;

	@ManyToOne
	private AppointmentDetails appointment;

	@ManyToOne
	private AppointmentServices service;

	@ManyToOne
	@JoinColumn(name = "work_order_id")
	private WorkOrder workOrder;

	@Column(name = "comments")
	private String comments;

}
