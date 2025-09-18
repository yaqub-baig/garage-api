package com.autoadda.apis.garage.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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