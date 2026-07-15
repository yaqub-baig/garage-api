package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.ExpertiseCategory;
import com.autoadda.apis.garage.response.ExpertiseCategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class ExpertiseCategoryMapper {

    public ExpertiseCategoryResponse toResponse(ExpertiseCategory entity) {
        if (entity == null) {
            return null;
        }
        return ExpertiseCategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }
}
