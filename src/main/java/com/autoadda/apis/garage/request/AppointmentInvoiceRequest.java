package com.autoadda.apis.garage.request;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentInvoiceRequest {
    private Integer appointmentId;
    private Integer garageId;
    private String status;
    private List<ServiceInvoice> service;
    private Double taxAmount;
    private String comments;
    private String taxId;
}
