package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class VehicleReceivedRequest {
    private Integer vehicleId;
    private Integer appointmentId;
    private Integer receiverId;
    private List<Long> vehicleImages= new ArrayList<>();
    private String comment;
    private Boolean isMainKeyReceived;
    private Boolean isSpareKeyReceived;
    private Boolean isToolKitReceived;
    private Boolean isJackReceived;
    private Boolean isWheelSpannerReceived;
    private Boolean isSpareTyreReceived;
    private Boolean isFireExtinguisherReceived;
    private Boolean isFirstAidKitReceived;
    private Boolean isCentralLockRemoteReceived;
    private Boolean isRCBookReceived;
    private Boolean isInsuranceCopyReceived;
    private Boolean isPUCReceived;
}