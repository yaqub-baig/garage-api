package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.util.VehicleRecievedStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class VehicleRecievedResponse {
    private Integer id;
    private VehicleDetailResponse vehicle;
    private AppointmentDetailResponse appointment;
    private TechnicianResponse recievedBy;
    private LocalDateTime recievedTime;
    private String comment;
    private List<MediaFile> vehicleRecievedImages;
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
    private VehicleRecievedStatus status;


}
