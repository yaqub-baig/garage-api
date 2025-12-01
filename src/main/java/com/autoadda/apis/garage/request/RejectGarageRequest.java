package com.autoadda.apis.garage.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectGarageRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 1, max = 1000, message = "Rejection reason must be between 1 and 1000 characters")
    private String rejectionReason;
}
