package com.service.garage.apis.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
