package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for technician expertise.
 * Used when creating or updating a technician's expertise areas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianExpertiseRequest {

    @NotNull(message = "Expertise category is required")
    private Integer expertiseCategoryId;

    @NotNull(message = "Expertise level is required")
    private ExpertiseLevel expertiseLevel;
}
