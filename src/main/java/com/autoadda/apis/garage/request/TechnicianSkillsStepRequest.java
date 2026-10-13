package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Step 4 of the technician CV profile. A null list leaves that part unchanged;
 * an empty list clears it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSkillsStepRequest {

    private List<Integer> skillSetIds;

    private List<TechnicianExpertiseRequest> expertise;

    private List<SkillRequest> skills;
}
