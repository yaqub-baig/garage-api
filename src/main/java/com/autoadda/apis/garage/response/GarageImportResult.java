package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarageImportResult {
    private int total;
    private int imported;
    private int skippedNonAutomotive;
    private int skippedDuplicatePhone;
    private int skippedInvalidTitle;
}
