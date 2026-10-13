package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ProfileStep;
import com.autoadda.apis.garage.util.ProfileStepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStepCompletionResponse {

    private ProfileStep step;
    private Integer order;
    private ProfileStepStatus status;
    private Integer percent;
}
