package com.autoadda.apis.garage.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class WorkOrder {
	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne
	private Garage garage;

	@OneToOne
	private AppointmentDetails appointment;

	@OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL)
	private List<JobCard> jobCards;

	@Enumerated(EnumType.STRING)
	private Status status;

	private LocalDateTime createdDate;

	
}
