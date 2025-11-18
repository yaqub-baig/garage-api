package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.DeliveryStatus;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderResponse {
    private Integer id;
    private GarageResponse garage;
    private AppointmentDetailResponse appointmentDetail;
    private List<JobCardResponse> jobCards;
    private WorkOrderStatus status;
    private Double totalAmount;
    private LocalDateTime createdDate;
    private Integer appointmentId;
    private String deliveryRemarks;
    private Boolean customerAcknowledged;
    private Double customerNegotiatedAmount1;
    private String customerNegotiatedComment;
    private Double garageNegotiatedAmount1;
    private String garageNegotiatedComment;
    private List<StatusTimelineEntry> statusTimeline;
    private Map<WorkOrderStatus, LocalDateTime> statusTimestamps;

    // Delivery status fields for frontend
    private DeliveryStatus currentDeliveryStatus;
    private Boolean canInitiateDelivery;
}
