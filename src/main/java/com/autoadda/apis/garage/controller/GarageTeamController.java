package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.ChangeMemberRoleRequest;
import com.autoadda.apis.garage.request.ChangeMemberStatusRequest;
import com.autoadda.apis.garage.request.GarageRoleRequest;
import com.autoadda.apis.garage.request.TeamInviteRequest;
import com.autoadda.apis.garage.response.GaragePermissionResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.service.GarageInviteService;
import com.autoadda.apis.garage.service.GarageRoleService;
import com.autoadda.apis.garage.service.GarageTeamService;
import com.autoadda.apis.garage.util.GaragePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static com.autoadda.apis.garage.util.GaragePermission.*;

/**
 * Garage team management: members, roles and invites. All endpoints act as the user in {@code X-Auto-Adda-Id}.
 */
@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
public class GarageTeamController {

    private static final String HEADER = "X-Auto-Adda-Id";
    private static final String BASE = "/garages/{garageId}/team";

    private final GarageTeamService teamService;
    private final GarageRoleService roleService;
    private final GarageInviteService inviteService;
    private final GarageAccessGuard guard;

    // ---------------------------------------------------------------- me

    @GetMapping(BASE + "/me")
    public ResponseEntity<GenericResponse> myAccess(@PathVariable Integer garageId,
                                                    @RequestHeader(value = HEADER, required = false) String caller) {
        return ok(teamService.myAccess(garageId, GarageAccessGuard.parseCaller(caller)));
    }

    // ---------------------------------------------------------------- members

    @GetMapping(BASE + "/members")
    public ResponseEntity<GenericResponse> members(@PathVariable Integer garageId,
                                                   @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_VIEW);
        return ok(teamService.listMembers(garageId));
    }

    @PutMapping(BASE + "/members/{memberId}/role")
    public ResponseEntity<GenericResponse> changeRole(@PathVariable Integer garageId, @PathVariable Integer memberId,
                                                      @RequestBody ChangeMemberRoleRequest request,
                                                      @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_MANAGE);
        return ok("Role updated",
                teamService.changeRole(garageId, memberId, request.getRoleId(), GarageAccessGuard.parseCaller(caller)));
    }

    @PutMapping(BASE + "/members/{memberId}/status")
    public ResponseEntity<GenericResponse> changeStatus(@PathVariable Integer garageId, @PathVariable Integer memberId,
                                                        @RequestBody ChangeMemberStatusRequest request,
                                                        @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_MANAGE);
        return ok("Status updated",
                teamService.changeStatus(garageId, memberId, request.getStatus(), GarageAccessGuard.parseCaller(caller)));
    }

    @DeleteMapping(BASE + "/members/{memberId}")
    public ResponseEntity<GenericResponse> removeMember(@PathVariable Integer garageId, @PathVariable Integer memberId,
                                                        @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_MANAGE);
        teamService.removeMember(garageId, memberId, GarageAccessGuard.parseCaller(caller));
        return ok("Member removed", null);
    }

    // ---------------------------------------------------------------- invites

    @PostMapping(BASE + "/invites")
    public ResponseEntity<GenericResponse> invite(@PathVariable Integer garageId, @RequestBody TeamInviteRequest request,
                                                  @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_MANAGE);
        return ok("Invite sent", inviteService.invite(garageId, request, GarageAccessGuard.parseCaller(caller)));
    }

    @GetMapping(BASE + "/invites")
    public ResponseEntity<GenericResponse> pendingInvites(@PathVariable Integer garageId,
                                                          @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_VIEW);
        return ok(inviteService.pendingInvites(garageId));
    }

    @DeleteMapping(BASE + "/invites/{inviteId}")
    public ResponseEntity<GenericResponse> cancelInvite(@PathVariable Integer garageId, @PathVariable Integer inviteId,
                                                        @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_MANAGE);
        inviteService.cancelInvite(garageId, inviteId);
        return ok("Invite cancelled", null);
    }

    /** Invites waiting for the calling user, across all garages. */
    @GetMapping("/team/invites/mine")
    public ResponseEntity<GenericResponse> myInvites(@RequestHeader(value = HEADER, required = false) String caller) {
        return ok(inviteService.myInvites(GarageAccessGuard.parseCaller(caller)));
    }

    // ---------------------------------------------------------------- roles

    @GetMapping(BASE + "/roles")
    public ResponseEntity<GenericResponse> roles(@PathVariable Integer garageId,
                                                 @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, TEAM_VIEW);
        return ok(roleService.listRoles(garageId));
    }

    @PostMapping(BASE + "/roles")
    public ResponseEntity<GenericResponse> createRole(@PathVariable Integer garageId, @RequestBody GarageRoleRequest request,
                                                      @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, ROLE_MANAGE);
        return ok("Role created", roleService.createRole(garageId, request, GarageAccessGuard.parseCaller(caller)));
    }

    @PutMapping(BASE + "/roles/{roleId}")
    public ResponseEntity<GenericResponse> updateRole(@PathVariable Integer garageId, @PathVariable Integer roleId,
                                                      @RequestBody GarageRoleRequest request,
                                                      @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, ROLE_MANAGE);
        return ok("Role updated", roleService.updateRole(garageId, roleId, request, GarageAccessGuard.parseCaller(caller)));
    }

    @DeleteMapping(BASE + "/roles/{roleId}")
    public ResponseEntity<GenericResponse> deleteRole(@PathVariable Integer garageId, @PathVariable Integer roleId,
                                                      @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, ROLE_MANAGE);
        roleService.deleteRole(garageId, roleId);
        return ok("Role deleted", null);
    }

    /** The permission catalogue for the role editor (same for every garage). */
    @GetMapping("/team/permissions")
    public ResponseEntity<GenericResponse> permissions() {
        List<GaragePermissionResponse> catalogue = Arrays.stream(GaragePermission.values())
                .map(p -> GaragePermissionResponse.builder()
                        .key(p.name()).group(p.getGroup()).description(p.getDescription()).build())
                .toList();
        return ok(catalogue);
    }

    private static ResponseEntity<GenericResponse> ok(Object body) {
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(body));
    }

    private static ResponseEntity<GenericResponse> ok(String message, Object body) {
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(message, body));
    }
}
