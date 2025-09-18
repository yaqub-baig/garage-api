package com.autoadda.apis.garage.request;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RevenueSummaryDTO {
    private LocalDate from;
    private LocalDate to;
    private double totalRevenue;
    private long totalJobs;
}