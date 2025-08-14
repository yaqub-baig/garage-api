package com.autoadda.apis.garage.entity;

import java.time.LocalDateTime;
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
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@ManyToOne
	@JoinColumn(name = "garage_id", referencedColumnName = "id")
	private Garage garage;

	@ManyToOne
	@JoinColumn(name = "technician_id", referencedColumnName = "id")
	private Technician technician;

	@ManyToOne
	@JoinColumn(name = "appointment_id", referencedColumnName = "id")
	private AppointmentDetails appointment;

	@ManyToOne
	@JoinColumn(name = "appointment_service_id", referencedColumnName = "id")
	private AppointmentServices service;

	@ManyToOne
	@JoinColumn(name = "work_order_id")
	private WorkOrder workOrder;

	@Column(name = "comments")
	private String comments;

}
