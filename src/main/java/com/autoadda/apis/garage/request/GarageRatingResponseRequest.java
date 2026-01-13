package com.autoadda.apis.garage.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GarageRatingResponseRequest {
    @NotNull(message = "Rating ID is required")
    private Integer garageRatingId;

    @NotBlank(message = "Response text is required")
    @Size(max = 2000, message = "Response text cannot exceed 2000 characters")
    private String responseText;
}
