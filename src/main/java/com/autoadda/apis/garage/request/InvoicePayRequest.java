package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.InvoiceStatus;
import com.autoadda.apis.garage.util.PaymentMethod;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvoicePayRequest {
    private Integer invoiceId;
    private InvoiceStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Double amountPaid;
    private String transactionId;
    private String comments;
}
