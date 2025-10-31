package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.PartRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestResponse {

    private Integer id;
    private Integer jobCardId;
    private String jobCardServiceName;
    private Integer requestedByTechnicianId;
    private String requestedByTechnicianName;
    private PartRequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime customerResponseDate;
    private Integer customerId;
    private Integer vehicleId;
    private String vehicleDetails; // e.g., "Honda City - KA01AB1234"
    private List<PartRequestItemResponse> items;
    private CustomerPartResponseResponse customerResponse;
    private String notes;
}
