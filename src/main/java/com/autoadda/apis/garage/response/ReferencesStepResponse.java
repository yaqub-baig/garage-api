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
public class ReferencesStepResponse {

    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private List<ReferenceResponse> references;
}
