package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private Integer id;
    private Integer technicianId;
    private Integer autoAddaId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String profileImg;
    private Integer roleId;
    private String roleName;
    private TeamMemberStatus status;
    private Boolean isOwner;
    private LocalDateTime joinedAt;
}
