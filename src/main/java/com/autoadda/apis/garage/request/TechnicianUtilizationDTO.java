package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TechnicianUtilizationDTO {
	private Integer technicianId;
	private String technicianName;
	private int jobsCompleted;
	private int availableHours;
	private double utilizationPercent;
}