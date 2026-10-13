package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ProfileCompletionStatus;
import com.autoadda.apis.garage.util.ProfileStep;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletionResponse {

    private Integer percent;
    private ProfileCompletionStatus status;
    private List<ProfileStepCompletionResponse> steps;
    private ProfileStep nextIncompleteStep;
    private LocalDateTime updatedAt;
}
