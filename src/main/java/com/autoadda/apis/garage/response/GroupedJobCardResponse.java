package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupedJobCardResponse {

    private Map<Integer, WorkOrderJobCardGroup> workOrderGroups;
    private Integer totalJobCards;
    private Integer totalWorkOrders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WorkOrderJobCardGroup {
        private Integer workOrderId;
        private WorkOrderResponse workOrderDetails;
        private List<JobCardResponse> jobCards;
        private Integer jobCardCount;
        private String workOrderStatus;

    }
}