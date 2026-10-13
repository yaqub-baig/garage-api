package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private Integer id;
    private Integer displayOrder;
    private String skillName;
    private ExpertiseLevel level;
    private Integer yearsOfExperience;
}
