package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

    private Integer workOrderId;
    private List<InvoiceLineItemRequest> lineItems;
    private Double taxRate; // e.g., 10.0 means 10%
    private Integer loyaltyPointsToUse;
    private String paymentMethod; // "CASH", "CREDIT_CARD", "ONLINE"
    private String notes;
}