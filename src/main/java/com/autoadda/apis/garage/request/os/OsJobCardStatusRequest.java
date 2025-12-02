package com.autoadda.apis.garage.request.os;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OsJobCardStatusRequest {
    private Integer jobcardId;
    private Status status;
    private Integer memberId;
    private String memberType;
    private String comments;
}
