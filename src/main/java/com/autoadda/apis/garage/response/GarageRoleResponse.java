package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.GaragePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageRoleResponse {
    private Integer id;
    private String name;
    private String description;
    private Set<GaragePermission> permissions;
    private Boolean isSystem;
    private Long memberCount;
    private Long pendingInviteCount;
}
