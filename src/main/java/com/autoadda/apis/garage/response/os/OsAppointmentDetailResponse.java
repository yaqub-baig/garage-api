package com.autoadda.apis.garage.response.os;

import com.autoadda.apis.garage.response.AvailableServiceResponse;
import com.autoadda.apis.garage.response.OsResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
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
public class OsAppointmentDetailResponse {

    private Integer id;
    private String appointmentReferenceId;
    private LocalDate scheduleDate;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private LocalTime deliveryTime;
    private Status status;
    private String comments;
    private OsResponse otherService;
    private String selectedOsServiceIds;
  private List<AvailableServiceResponse> osServicesResponse;
    private String otherServiceSubscriptionPlanId;
    private Integer customerId;
    private Integer vehicleId;
    private OsAppointmentSlotResponse slot;
    private Integer technicianId;
    private VehicleDetailResponse vehicle;
}
