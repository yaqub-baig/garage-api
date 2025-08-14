package com.autoadda.apis.garage.entity;

import java.math.BigDecimal;
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
public class InsuranceClaim {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer vehicleOwnerId;
	private Integer garageId;
	private Integer jobCardId; // Optional: link to a repair job
	private String policyNumber;
	private String insurerName;
	private String vehicleNumber;

	private String claimDescription;
	private LocalDateTime requestDate;

	@Enumerated(EnumType.STRING)
	private Status status;

	private String approvalRemarks;

	private BigDecimal estimatedAmount;
	private BigDecimal approvedAmount;

	private LocalDateTime updatedAt;
}
