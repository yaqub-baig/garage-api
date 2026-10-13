package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.CareerBreakReason;
import com.autoadda.apis.garage.util.EmploymentEntryType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentResponse {

    private Integer id;
    private Integer displayOrder;
    private EmploymentEntryType entryType;
    private CareerBreakReason breakReason;
    private String breakDescription;
    private String employerName;
    private String jobTitle;
    private String city;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String responsibilities;
    private String reasonForLeaving;
}
