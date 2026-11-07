package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaragePermissionResponse {
    private String key;
    private String group;
    private String description;
}
