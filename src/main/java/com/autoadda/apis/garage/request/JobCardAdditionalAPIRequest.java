package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobCardAdditionalAPIRequest {
    private Integer requestId;
    private Integer jobcardId;
    private Integer garageId;
    private Integer technicianId;
    private Integer customerId;
    private Status status;
    private String requestType;
    private String comments;
}
