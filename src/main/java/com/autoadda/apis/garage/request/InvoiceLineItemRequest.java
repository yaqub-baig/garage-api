package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItemRequest {

    private String description;
    private Integer quantity;
    private Double price;
    private String category; // SERVICE, PART, LABOR, etc.
    private Integer serviceId; // Optional reference to service
    private String notes;
}