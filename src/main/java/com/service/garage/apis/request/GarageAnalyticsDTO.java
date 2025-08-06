package com.service.garage.apis.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
public class GarageAnalyticsDTO {
	private LocalDate date;
	private long jobsToday;
	private BigDecimal revenueToday;
	private List<TechnicianUtilizationDTO> technicianUtilizations;
}