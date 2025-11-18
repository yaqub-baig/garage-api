package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.DeliveryStatus;
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
public class VehicleDeliveryResponse {

    private Integer id;
    private Integer workOrderId;
    private Integer deliveredByTechnicianId;
    private String deliveredByTechnicianName;
    private DeliveryStatus status;
    private LocalDateTime deliveryDate;
    private Boolean customerApproved;
    private String customerComments;
    private String rejectionReason;
    private List<String> deliveryPhotos; // URLs

    // Re-verification of 12 items
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
