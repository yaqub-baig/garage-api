package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReceivedChecklistResponse {

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
