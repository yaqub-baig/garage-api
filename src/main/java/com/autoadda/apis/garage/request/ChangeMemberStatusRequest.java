package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMemberStatusRequest {
    /** ACTIVE or SUSPENDED. Use DELETE /members/{id} to remove someone. */
    private TeamMemberStatus status;
}
