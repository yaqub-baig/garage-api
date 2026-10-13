package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A period with no job and no career break in a technician's work history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentGapResponse {

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer months;

    // True when the gap runs from the last entry until today (no current job or break)
    private Boolean ongoing;
}
