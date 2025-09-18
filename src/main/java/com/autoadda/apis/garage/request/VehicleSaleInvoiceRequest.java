package com.autoadda.apis.garage.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleInvoiceRequest {

    private Integer id;

    private LocalDate invoiceDate;

    private double totalAmount;

    private String paymentMethod;

    private VehicleSaleRequest vehicleSale;
}
