package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianAvailabilityRequest {

    private Integer id;

    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private TechnicianRequest technician;
    private Integer technicianId;

    // Getters and setters
}
