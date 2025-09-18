package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCardResponse {

    private Integer id;
    private LocalTime serviceStartTime;
    private LocalTime serviceEndTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer garageId;
    private Integer technicianId;
    private Integer appointmentId;
    private AppointmentServiceResponse appointmentService;
    private Integer workOrderId;
    private String comments;
    private List<JobCardStatusTimelineEntry> statusTimeline;
    private Map<JobCardStatus, LocalDateTime> statusTimestamps;
}
