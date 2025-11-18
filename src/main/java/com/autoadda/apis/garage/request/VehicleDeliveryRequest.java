package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDeliveryRequest {

    private Integer deliveredByTechnicianId;
    private List<Long> deliveryPhotos = new ArrayList<>();
    private String comments;

    // Re-verification of 12 items from vehicle receiving
    private Boolean isMainKeyReturned;
    private Boolean isSpareKeyReturned;
    private Boolean isToolKitReturned;
    private Boolean isJackReturned;
    private Boolean isWheelSpannerReturned;
    private Boolean isSpareTyreReturned;
    private Boolean isFireExtinguisherReturned;
    private Boolean isFirstAidKitReturned;
    private Boolean isCentralLockRemoteReturned;
    private Boolean isRCBookReturned;
    private Boolean isInsuranceCopyReturned;
    private Boolean isPUCReturned;
}
