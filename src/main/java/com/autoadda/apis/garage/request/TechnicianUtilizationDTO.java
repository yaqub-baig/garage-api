package com.autoadda.apis.garage.request;

import lombok.*;

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