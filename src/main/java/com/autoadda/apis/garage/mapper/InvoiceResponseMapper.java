package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.response.InvoiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceResponseMapper {

    private final InvoiceLineItemMapper lineItemMapper;
    private final GarageSubscriptionRepository garageSubscriptionRepository;

    public InvoiceResponse toResponse(Invoice invoice) {
        if ( invoice == null ) return null;

        // Extract subscription information if available
        Integer subscriptionPlanId = null;
        String subscriptionPlanName = null;

        if (invoice.getWorkOrder() != null && invoice.getWorkOrder().getAppointment() != null) {
            AppointmentDetails appointment = invoice.getWorkOrder().getAppointment();
            if (appointment.getGarageSubscriptionPlanId() != null) {
                try {
                    subscriptionPlanId = Integer.parseInt(appointment.getGarageSubscriptionPlanId());
                    GarageSubscription plan = garageSubscriptionRepository
                            .findById(subscriptionPlanId)
                            .orElse(null);
                    if (plan != null) {
                        subscriptionPlanName = plan.getName();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid subscription plan ID
                }
            }
        }

        return InvoiceResponse.builder ( )
                .id ( invoice.getId ( ) )
                .workOrderId ( invoice.getWorkOrder ( ) != null ? invoice.getWorkOrder ( ).getId ( ) : null )
                .jobCardId ( invoice.getJobCard ( ) != null ? invoice.getJobCard ( ).getId ( ) : null )
                .lineItems ( lineItemMapper.toResponseList ( invoice.getLineItems ( ) ) )
                .subtotal ( invoice.getSubtotal ( ) )
                .subscriptionDiscount ( invoice.getSubscriptionDiscount ( ) )
                .totalAmount ( invoice.getTotalAmount ( ) )
                .taxRate ( invoice.getTaxRate ( ) )
                .taxAmount ( invoice.getTaxAmount ( ) )
                .amountPaid ( invoice.getAmountPaid ( ) )
                .subscriptionPlanId ( subscriptionPlanId )
                .subscriptionPlanName ( subscriptionPlanName )
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