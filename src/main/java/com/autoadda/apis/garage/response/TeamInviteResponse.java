package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.SendBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInviteResponse {
    /** Request id; accept/reject it via /requestingGarageTechnician/technician/{id}/approve|reject. */
    private Integer id;
    private Integer garageId;
    private String garageName;
    private String phone;
    /** Null until the invited person has an AutoAdda account. */
    private Integer technicianId;
    private String inviteeName;
    private Integer roleId;
    private String roleName;
    private String message;
    private SendBy sendBy;
    private ApprovalStatus status;
    private Integer invitedBy;
    private LocalDateTime invitedAt;
}
