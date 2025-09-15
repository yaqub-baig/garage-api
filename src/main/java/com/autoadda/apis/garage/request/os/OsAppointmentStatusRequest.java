package com.autoadda.apis.garage.request.os;

import com.autoadda.apis.garage.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OsAppointmentStatusRequest {
    private Integer appointmentId;
    private Integer memberId;
    private String memberType;
    private Status status;
    private String comments;
}
