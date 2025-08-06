package com.service.garage.apis.request;

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
public class TechnicianSummaryDTO {
	private String technicianName;
	private long totalJobs;
	private double totalRevenue;
	private double utilizationPercentage;
}