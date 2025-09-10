package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentDetailResponse {

    private Integer id;
    private String appointmentReferenceId;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private Status status;
    private String comments;
    private GarageResponse garage;
    private String selectedGarageServiceIds;
    private List<AvailableServiceResponse> garageServicesResponse;
    private String garageSubscriptionPlanId;
    private Integer customerId;
    private Integer vehicleId;
    private AppointmentSlotResponse slot;
    private Integer technicianId;
    private VehicleDetailResponse vehicle;
}
