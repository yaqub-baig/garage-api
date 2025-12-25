package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.SendBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestingGarageTechnicianResponse {

    private Integer id;

    // Nested garage details (lightweight response)
    private GarageResponse garage;

    // Nested technician details (lightweight response)
    private TechnicianResponse technician;

    // Who sent this request
    private SendBy sendBy;

    // Current status of the request
    private ApprovalStatus approvalStatus;

    // When the request was created
    private LocalDateTime requestedAt;

    // Optional description/message
    private String description;
}
