package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusTimelineEntry {
    private WorkOrderStatus status;
    private LocalDateTime timestamp;
    private String description;
    private Integer durationInMinutes; // Duration from previous status (if applicable)
}