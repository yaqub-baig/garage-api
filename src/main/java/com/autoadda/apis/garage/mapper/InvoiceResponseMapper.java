package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.response.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceResponseMapper {

    private final InvoiceLineItemMapper lineItemMapper;

    public InvoiceResponse toResponse(Invoice invoice) {
        if ( invoice == null ) return null;

        return InvoiceResponse.builder ( )
                .id ( invoice.getId ( ) )
                .workOrderId ( invoice.getWorkOrder ( ) != null ? invoice.getWorkOrder ( ).getId ( ) : null )
                .jobCardId ( invoice.getJobCard ( ) != null ? invoice.getJobCard ( ).getId ( ) : null )
                .lineItems ( lineItemMapper.toResponseList ( invoice.getLineItems ( ) ) )
                .totalAmount ( invoice.getTotalAmount ( ) )
                .taxRate ( invoice.getTaxRate ( ) )
                .taxAmount ( invoice.getTaxAmount ( ) )
                .amountPaid ( invoice.getAmountPaid ( ) )
                .loyaltyPointsUsed ( invoice.getLoyaltyPointsUsed ( ) )
                .loyaltyPointsEarned ( invoice.getLoyaltyPointsEarned ( ) )
                .paymentStatus ( invoice.getPaymentStatus ( ) )
                .paymentMethod ( invoice.getPaymentMethod ( ) )
                .isPaid ( invoice.getIsPaid ( ) )
                .notes ( invoice.getNotes ( ) )
                .createdAt ( invoice.getCreatedAt ( ) )
                .build ( );
    }
}