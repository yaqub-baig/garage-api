package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentInvoiceResponse {
    private Integer id;
    private String status;
    private String comments;
    private LocalDate insertTms;
    private Double netAmount;
    private Double taxAmount;
    private Double totalAmount;
    private String taxId;
    private GarageResponse garage;
    private AppointmentDetailResponse appointment;
}