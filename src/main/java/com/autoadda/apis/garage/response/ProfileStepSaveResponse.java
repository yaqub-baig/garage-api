package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of saving one step of the technician CV profile:
 * the saved section plus the recalculated completion, so the UI can refresh its progress bar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStepSaveResponse<T> {

    private T data;

    private ProfileCompletionResponse completion;
}
