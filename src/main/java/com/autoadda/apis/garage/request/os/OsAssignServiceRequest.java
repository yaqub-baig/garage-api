package com.autoadda.apis.garage.request.os;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OsAssignServiceRequest {
    private Integer appointmentId;
    private Integer serviceId;
    private Integer technicianId;
    private Integer memberId;
    private String memberType;
    private Integer otherServiceId;
    private String status;
    private String comments;
}
