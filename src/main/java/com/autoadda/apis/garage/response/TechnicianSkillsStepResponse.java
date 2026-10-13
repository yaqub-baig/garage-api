package com.autoadda.apis.garage.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianSkillsStepResponse {

    private List<AvailableServiceResponse> skillSet;
    private List<TechnicianExpertiseResponse> expertise;
    private List<SkillResponse> skills;
}
