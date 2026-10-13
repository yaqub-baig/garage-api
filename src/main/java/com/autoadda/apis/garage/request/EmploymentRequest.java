package com.autoadda.apis.garage.request;

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
public class EmploymentRequest implements ProfileItemRequest {

    private Integer id;
    private EmploymentEntryType entryType;      // EMPLOYMENT (default when null) or CAREER_BREAK
    private CareerBreakReason breakReason;      // CAREER_BREAK only
    private String breakDescription;            // CAREER_BREAK only
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
