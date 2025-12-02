package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.InvoiceStatus;
import com.autoadda.apis.garage.util.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Integer id;
    private Integer workOrderId;
    private Integer jobCardId;
    private List<InvoiceLineItemResponse> lineItems;

    private Double totalAmount;
    private Double taxRate;
    private Double taxAmount;
    private Double amountPaid;

    private Integer loyaltyPointsUsed;
    private Integer loyaltyPointsEarned;

    private InvoiceStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Boolean isPaid;
    private String notes;

    private LocalDateTime createdAt;
}