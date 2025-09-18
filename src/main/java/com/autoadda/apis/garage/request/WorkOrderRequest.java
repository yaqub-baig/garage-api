package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrderRequest {
    private List<AssignServiceRequest> assignServiceRequestList;
    private Integer appointmentId;
    private WorkOrderStatus status;
    private String deliveryRemarks;
    private Boolean customerAcknowledged;
    private Double customerNegotiatedAmount;
    private String customerNegotiatedComment;
    private Double garageNegotiatedAmount;
    private String garageNegotiatedComment;
}
