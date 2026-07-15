package com.autoadda.apis.garage.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpertiseCategoryRequest {

    @NotBlank(message = "Expertise category name is required")
    private String name;

    private String description;
}
