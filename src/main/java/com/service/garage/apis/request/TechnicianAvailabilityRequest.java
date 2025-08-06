package com.service.garage.apis.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.service.garage.apis.util.Status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAvailabilityRequest {

	private Integer id;

	private LocalDate date;

	private LocalTime startTime;
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	private Status status;

	private TechnicianRequest technician;
	private Integer technicianId;

	// Getters and setters
}
