package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobCardStatusRequest {
    private Integer jobcardId;
    private JobCardStatus status;
    private Integer memberId;
    private String memberType;
    private String comments;
}
