package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.ExpertiseCategory;
import com.autoadda.apis.garage.entity.TechnicianExpertise;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.ExpertiseCategoryRepository;
import com.autoadda.apis.garage.request.TechnicianExpertiseRequest;
import com.autoadda.apis.garage.response.TechnicianExpertiseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TechnicianExpertise entity and DTOs.
 */
@Component
@RequiredArgsConstructor
public class TechnicianExpertiseMapper {

    private final ExpertiseCategoryRepository expertiseCategoryRepository;
    private final ExpertiseCategoryMapper expertiseCategoryMapper;

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
                .expertiseCategory(expertiseCategoryMapper.toResponse(entity.getExpertiseCategory()))
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

        ExpertiseCategory expertiseCategory = expertiseCategoryRepository.findById(request.getExpertiseCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expertise category not found with id: " + request.getExpertiseCategoryId()));

        return TechnicianExpertise.builder()
                .expertiseCategory(expertiseCategory)
                .expertiseLevel(request.getExpertiseLevel())
                .build();
    }
}
