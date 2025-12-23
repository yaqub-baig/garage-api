package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // WebPush preferences (mirrors identity-api entity structure)
    private Boolean webpushOnServiceUpdates;
    private Boolean webpushOnAppointments;
    private Boolean webpushOnReminders;
    private Boolean webpushOnOffers;
    private Boolean webpushOnQuotationRequests;

    @Enumerated(EnumType.STRING)
    private Status status;

}
