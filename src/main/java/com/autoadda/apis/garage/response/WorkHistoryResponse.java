package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Work history step: jobs and career breaks, plus gaps that are not explained by either.
 * Gaps are informational only; the UI can offer to add a career break for them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkHistoryResponse {

    private List<EmploymentResponse> entries;

    private List<EmploymentGapResponse> unexplainedGaps;

    private Integer gapThresholdMonths;
}
