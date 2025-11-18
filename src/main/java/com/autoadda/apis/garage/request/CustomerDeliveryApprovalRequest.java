package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDeliveryApprovalRequest {

    private Boolean approved;
    private String customerComments;
    private String rejectionReason; // Required if approved = false
}
