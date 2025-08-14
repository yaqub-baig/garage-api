package com.autoadda.apis.garage.entity;

import java.time.LocalDateTime;

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
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadsideAssistance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer vehicleOwnerId;

	private String vehicleNumber;

	private String issueDescription;

	private String location;

	private LocalDateTime requestTime;

	@Enumerated(EnumType.STRING)
	private Status status;

	private String assignedTechnician;

	private String contactNumber;

	private boolean isTowed;

	private String remarks;

	private LocalDateTime completedAt;
}
