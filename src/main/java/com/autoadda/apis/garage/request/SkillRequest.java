package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequest implements ProfileItemRequest {

    private Integer id;
    private String skillName;
    private ExpertiseLevel level;
    private Integer yearsOfExperience;
}
