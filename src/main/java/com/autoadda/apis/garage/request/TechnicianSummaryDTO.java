package com.autoadda.apis.garage.request;

import lombok.*;

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