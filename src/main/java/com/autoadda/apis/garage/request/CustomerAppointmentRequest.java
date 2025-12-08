package com.autoadda.apis.garage.request;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomerAppointmentRequest {
    private Integer customerId;
    private Integer garageId;
    private Integer vehicleId;
    private List<Integer> serviceIds;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private String comments;
    private Integer memberId;
    private String memberType;
    private Integer vehicleOwnerSubscriptionId; // Optional: ID of active subscription to use


}
