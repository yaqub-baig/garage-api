package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDeliveryComparisonResponse {

    private Integer workOrderId;
    private Integer deliveryId;
    // Receiving checklist (from VehicleReceived)
    private ItemsChecklist receivingChecklist;
    // Delivery checklist (from VehicleDelivery)
    private ItemsChecklist deliveryChecklist;
    // List of mismatched items
    private List<String> mismatches;

    // Photos comparison
    private List<String> receivingPhotos; // URLs from vehicle receiving
    private List<String> deliveryPhotos;  // URLs from vehicle delivery

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemsChecklist {
        private Boolean isMainKey;
        private Boolean isSpareKey;
        private Boolean isToolKit;
        private Boolean isJack;
        private Boolean isWheelSpanner;
        private Boolean isSpareTyre;
        private Boolean isFireExtinguisher;
        private Boolean isFirstAidKit;
        private Boolean isCentralLockRemote;
        private Boolean isRCBook;
        private Boolean isInsuranceCopy;
        private Boolean isPUC;
    }
}
