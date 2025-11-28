package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.WorkOrderStatus;
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
public class VehicleServiceHistoryResponse {

    private Integer workOrderId;
    private LocalDateTime serviceDate;
    private WorkOrderStatus status;

    // Garage Information
    private GarageBasicInfo garage;

    // Services Performed
    private List<String> servicesPerformed;

    // Invoice and Payment Details
    private InvoiceResponse invoice;

    // Latest Delivery Information
    private LatestDeliveryInfo latestDelivery;
}
