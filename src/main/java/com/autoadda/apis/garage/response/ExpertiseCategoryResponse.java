package com.autoadda.apis.garage.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpertiseCategoryResponse {

    private Integer id;
    private String name;
    private String description;
    private Boolean isActive;
}
