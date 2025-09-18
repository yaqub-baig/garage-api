package com.autoadda.apis.garage.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionReportRequest {

    private Integer id;

    private LocalDate inspectionDate;

    private String inspectorName;

    private String remarks;

    private Boolean passed;

    private VehicleSaleRequest vehicleSale;
}
