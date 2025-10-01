package com.autoadda.apis.garage.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssignServiceRequest {
    private Integer appointmentId;
    private Integer serviceId;
    private Integer technicianId;
    private Integer memberId;
    private String memberType;
    private Integer garageId;
    private String status;
    private String comments;
}
