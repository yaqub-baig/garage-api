package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for technician expertise.
 * Used when returning expertise data in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianExpertiseResponse {

    private Integer id;

    private ExpertiseCategoryResponse expertiseCategory;

    private ExpertiseLevel expertiseLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
