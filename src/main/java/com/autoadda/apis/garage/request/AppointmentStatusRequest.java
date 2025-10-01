package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentStatusRequest {
    private Integer appointmentId;
    private Integer memberId;
    private String memberType;
    private Status status;
    private String comments;
}
