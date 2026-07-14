package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBusinessSaleInvoiceResponse {
    private Integer id;
    private Integer saleId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private Double totalAmount;
    private String paymentMethod;
}
