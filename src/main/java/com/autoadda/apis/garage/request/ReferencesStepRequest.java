package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Step 8 of the technician CV profile. Null emergency contact fields are left unchanged;
 * a null references list leaves references unchanged, an empty list clears them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferencesStepRequest {

    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;

    private List<ReferenceRequest> references;
}
