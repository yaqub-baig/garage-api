package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleOwnerSubscriptionResponse {
    private Integer id;
    private UserResponse customer;
    private GarageSubscriptionResponse garageSubscription;
    private LocalDate purchaseDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private String usageCounts; // Deserialized from JSON string
}
