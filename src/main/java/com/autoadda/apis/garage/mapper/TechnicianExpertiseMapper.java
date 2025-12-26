package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.TechnicianExpertise;
import com.autoadda.apis.garage.request.TechnicianExpertiseRequest;
import com.autoadda.apis.garage.response.TechnicianExpertiseResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TechnicianExpertise entity and DTOs.
 */
@Component
public class TechnicianExpertiseMapper {

    /**
     * Convert TechnicianExpertise entity to response DTO.
     *
     * @param entity the entity to convert
     * @return the response DTO, or null if entity is null
     */
    public TechnicianExpertiseResponse toResponse(TechnicianExpertise entity) {
        if (entity == null) {
            return null;
        }

        return TechnicianExpertiseResponse.builder()
                .id(entity.getId())
                .expertiseCategory(entity.getExpertiseCategory())
                .expertiseLevel(entity.getExpertiseLevel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convert TechnicianExpertiseRequest DTO to entity.
     * Note: The technician relationship must be set separately.
     *
     * @param request the request DTO to convert
     * @return the entity, or null if request is null
     */
    public TechnicianExpertise toEntity(TechnicianExpertiseRequest request) {
        if (request == null) {
            return null;
        }

        return TechnicianExpertise.builder()
                .expertiseCategory(request.getExpertiseCategory())
                .expertiseLevel(request.getExpertiseLevel())
                .build();
    }
}
