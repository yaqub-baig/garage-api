package com.service.garage.apis.request;

import com.service.garage.apis.util.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationPreferenceRequest {

    private Integer id;

    private Integer customerId; // Reference to the customer (could be VehicleOwner or User)

    private Boolean emailOnServiceUpdates;
    private Boolean smsOnServiceUpdates;

    private Boolean emailOnAppointments;
    private Boolean smsOnAppointments;

    private Boolean emailOnReminders;
    private Boolean smsOnReminders;

    private Boolean emailOnOffers;
    private Boolean smsOnOffers;

    private Boolean emailOnQuotationRequests;
    private Boolean smsOnQuotationRequests;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Getters and setters
}
