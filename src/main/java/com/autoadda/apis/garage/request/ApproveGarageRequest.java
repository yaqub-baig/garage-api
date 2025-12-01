package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveGarageRequest {
    private String notes;  // Optional notes for approval
}
