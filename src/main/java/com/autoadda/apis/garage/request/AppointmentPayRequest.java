package com.autoadda.apis.garage.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentPayRequest {
    private Integer appointmentId;
    private String status;
    private String comments;
}
