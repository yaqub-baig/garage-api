package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamInviteRequest {
    /** Phone number the person uses (or will use) to sign in to AutoAdda. */
    private String phone;
    private Integer roleId;
    /** Optional message shown in the invite. */
    private String message;
}
