package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.GaragePermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageRoleRequest {
    private String name;
    private String description;
    private Set<GaragePermission> permissions;
}
