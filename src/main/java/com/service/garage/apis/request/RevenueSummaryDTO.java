package com.service.garage.apis.request;

import java.time.LocalDate;

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
public class RevenueSummaryDTO {
    private LocalDate from;
    private LocalDate to;
    private double totalRevenue;
    private long totalJobs;
}