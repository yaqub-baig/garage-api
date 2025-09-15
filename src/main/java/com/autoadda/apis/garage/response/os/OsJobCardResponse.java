package com.autoadda.apis.garage.response.os;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsJobCardResponse {

    private Integer id;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer otherServiceId;
    private Integer technicianId;
    private Integer appointmentId;
    private Integer appointmentServiceId;
    private Integer workOrderId;
    private String comments;
}
