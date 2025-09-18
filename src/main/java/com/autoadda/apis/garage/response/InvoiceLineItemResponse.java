package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItemResponse {

    private Integer id;
    private String description;
    private Integer quantity;
    private Double price;
    private Double totalAmount; // price * quantity
    private String category;
    private Integer serviceId;
    private String notes;
}