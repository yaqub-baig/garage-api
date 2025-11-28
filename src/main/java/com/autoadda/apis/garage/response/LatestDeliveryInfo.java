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
public class LatestDeliveryInfo {

    private Integer id;
    private DeliveryStatus status;
    private LocalDateTime deliveryDate;
    private Boolean customerApproved;
    private String customerComments;
    private String rejectionReason;
    private Integer attemptNumber;
    private List<String> deliveryPhotos;
}
