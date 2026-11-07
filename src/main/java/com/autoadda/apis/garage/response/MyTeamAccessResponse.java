package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamAccessResponse {
    private Integer garageId;
    private Boolean isOwner;
    private Boolean isMember;
    private Integer memberId;
    private Integer roleId;
    private String roleName;
    private TeamMemberStatus status;
    /** What the caller may do right now (empty when suspended or not a member). */
    private Set<GaragePermission> permissions;
}
