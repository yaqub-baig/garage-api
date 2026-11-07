package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageRoleRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.RequestingGarageTechnicianRepository;
import com.autoadda.apis.garage.request.GarageRoleRequest;
import com.autoadda.apis.garage.response.GarageRoleResponse;
import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.autoadda.apis.garage.util.GaragePermission.*;

/**
 * Garage-defined roles. Every garage starts with Owner (system, read-only) plus editable
 * Admin / Manager / HR / Technician templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageRoleService {

    public static final String OWNER = "Owner";
    public static final String ADMIN = "Admin";
    public static final String MANAGER = "Manager";
    public static final String HR = "HR";
    public static final String TECHNICIAN = "Technician";

    private final GarageRoleRepository roleRepository;
    private final GarageRepository garageRepository;
    private final GarageTeamMemberRepository memberRepository;
    private final RequestingGarageTechnicianRepository requestRepository;
    private final GarageAccessGuard guard;

    /** Default roles created for every garage. Order matters: Owner first. */
    static Map<String, Set<GaragePermission>> defaultRoles() {
        Map<String, Set<GaragePermission>> roles = new LinkedHashMap<>();
        roles.put(OWNER, EnumSet.allOf(GaragePermission.class));
        roles.put(ADMIN, EnumSet.complementOf(EnumSet.of(ROLE_MANAGE)));
        roles.put(MANAGER, EnumSet.of(TEAM_VIEW, APPOINTMENT_VIEW, APPOINTMENT_MANAGE, WORK_ORDER_MANAGE,
                JOB_CARD_ASSIGN, JOB_CARD_UPDATE, VEHICLE_CHECKIN_DELIVERY, PART_REQUEST_MANAGE,
                INVENTORY_VIEW, INVENTORY_MANAGE, TOOLS_MANAGE, INVOICE_VIEW, INVOICE_MANAGE, ANALYTICS_VIEW));
        roles.put(HR, EnumSet.of(TEAM_VIEW, TEAM_MANAGE));
        roles.put(TECHNICIAN, EnumSet.of(APPOINTMENT_VIEW, JOB_CARD_UPDATE, VEHICLE_CHECKIN_DELIVERY,
                PART_REQUEST_MANAGE, INVENTORY_VIEW));
        return roles;
    }

    private static final Map<String, String> DEFAULT_DESCRIPTIONS = Map.of(
            OWNER, "Full access. Cannot be changed.",
            ADMIN, "Everything except editing roles",
            MANAGER, "Runs day-to-day operations, inventory and invoicing",
            HR, "Manages the team",
            TECHNICIAN, "Works on assigned job cards");

    /** Creates the default roles for a garage if it has none yet. Safe to call repeatedly. */
    @Transactional
    public void seedDefaultRoles(Garage garage) {
        if (roleRepository.existsByGarageId(garage.getId())) {
            return;
        }
        defaultRoles().forEach((name, permissions) -> roleRepository.save(GarageRole.builder()
                .garage(garage)
                .name(name)
                .description(DEFAULT_DESCRIPTIONS.get(name))
                .permissions(new HashSet<>(permissions))
                .isSystem(OWNER.equals(name))
                .build()));
        log.info("Seeded default team roles for garage {}", garage.getId());
    }

    @Transactional
    public GarageRole ownerRole(Garage garage) {
        seedDefaultRoles(garage);
        return roleRepository.findByGarageIdOrderByIdAsc(garage.getId()).stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsSystem()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Owner role missing for garage " + garage.getId()));
    }

    /**
     * Role given to people who join without one being chosen (technician join requests, direct adds).
     * Uses "Technician" if it still exists, otherwise the non-system role with the fewest permissions.
     */
    @Transactional
    public GarageRole defaultMemberRole(Garage garage) {
        seedDefaultRoles(garage);
        List<GarageRole> roles = roleRepository.findByGarageIdOrderByIdAsc(garage.getId());
        return roles.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsSystem()) && TECHNICIAN.equalsIgnoreCase(r.getName()))
                .findFirst()
                .or(() -> roles.stream()
                        .filter(r -> !Boolean.TRUE.equals(r.getIsSystem()))
                        .min(Comparator.comparingInt(r -> r.getPermissions().size())))
                .orElseGet(() -> roleRepository.save(GarageRole.builder()
                        .garage(garage).name(TECHNICIAN).description(DEFAULT_DESCRIPTIONS.get(TECHNICIAN))
                        .permissions(new HashSet<>(defaultRoles().get(TECHNICIAN))).isSystem(false).build()));
    }

    /** The garage's role with this name, or the default member role if it was renamed/deleted. */
    @Transactional
    public GarageRole roleNamed(Garage garage, String name) {
        seedDefaultRoles(garage);
        return roleRepository.findByGarageIdAndNameIgnoreCase(garage.getId(), name)
                .filter(r -> !Boolean.TRUE.equals(r.getIsSystem()))
                .orElseGet(() -> defaultMemberRole(garage));
    }

    /** A role of this garage that can be handed out (i.e. not the Owner role). */
    @Transactional(readOnly = true)
    public GarageRole assignableRole(Integer garageId, Integer roleId) {
        if (roleId == null) {
            throw new BadRequestException("roleId is required");
        }
        GarageRole role = roleRepository.findByIdAndGarageId(roleId, garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found in this garage: " + roleId));
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BadRequestException("The Owner role can't be assigned");
        }
        return role;
    }

    /** Non-owners can't hand out (or define) permissions they don't hold themselves. */
    public void assertCanGrant(Integer actorAutoAddaId, Integer garageId, Set<GaragePermission> permissions) {
        Set<GaragePermission> own = guard.effectivePermissions(actorAutoAddaId, garageId);
        if (!own.containsAll(permissions)) {
            Set<GaragePermission> missing = EnumSet.copyOf(permissions);
            missing.removeAll(own);
            throw new ForbiddenException("You can't grant permissions you don't have: " + missing);
        }
    }

    /** Permissions that control the team itself; only people who hold them can hand them out. */
    private static final Set<GaragePermission> TEAM_CONTROL = EnumSet.of(TEAM_MANAGE, ROLE_MANAGE);

    /**
     * Check for giving someone a role (invite, role change, managing a member). Anyone with TEAM_MANAGE can
     * assign operational roles, but team-control permissions (TEAM_MANAGE, ROLE_MANAGE) can only be handed
     * out by someone who holds them, so HR can hire staff without being able to create new role editors.
     */
    public void assertCanAssign(Integer actorAutoAddaId, Integer garageId, Set<GaragePermission> rolePermissions) {
        Set<GaragePermission> control = EnumSet.noneOf(GaragePermission.class);
        rolePermissions.stream().filter(TEAM_CONTROL::contains).forEach(control::add);
        if (!control.isEmpty()) {
            assertCanGrant(actorAutoAddaId, garageId, control);
        }
    }

    @Transactional
    public List<GarageRoleResponse> listRoles(Integer garageId) {
        seedDefaultRoles(requireGarage(garageId));
        return roleRepository.findByGarageIdOrderByIdAsc(garageId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public GarageRoleResponse createRole(Integer garageId, GarageRoleRequest request, Integer actorAutoAddaId) {
        Garage garage = requireGarage(garageId);
        seedDefaultRoles(garage);
        String name = validName(request.getName());
        ensureNameFree(garageId, name, null);
        Set<GaragePermission> permissions = permissionsOf(request);
        assertCanGrant(actorAutoAddaId, garageId, permissions);

        GarageRole role = roleRepository.save(GarageRole.builder()
                .garage(garage)
                .name(name)
                .description(trimToNull(request.getDescription()))
                .permissions(new HashSet<>(permissions))
                .isSystem(false)
                .build());
        log.info("Garage {}: role '{}' created by {}", garageId, name, actorAutoAddaId);
        return toResponse(role);
    }

    @Transactional
    public GarageRoleResponse updateRole(Integer garageId, Integer roleId, GarageRoleRequest request, Integer actorAutoAddaId) {
        GarageRole role = editableRole(garageId, roleId);
        if (request.getName() != null) {
            String name = validName(request.getName());
            ensureNameFree(garageId, name, roleId);
            role.setName(name);
        }
        if (request.getDescription() != null) {
            role.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getPermissions() != null) {
            Set<GaragePermission> permissions = permissionsOf(request);
            assertCanGrant(actorAutoAddaId, garageId, permissions);
            role.getPermissions().clear();
            role.getPermissions().addAll(permissions);
        }
        log.info("Garage {}: role {} updated by {}", garageId, roleId, actorAutoAddaId);
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Integer garageId, Integer roleId) {
        GarageRole role = editableRole(garageId, roleId);
        long members = memberRepository.countByRoleIdAndStatusNot(roleId, TeamMemberStatus.REMOVED);
        long invites = requestRepository.countByRoleIdAndApprovalStatus(roleId, ApprovalStatus.HOLD);
        if (members > 0 || invites > 0) {
            throw new ConflictException(String.format(
                    "Role '%s' is still used by %d member(s) and %d pending invite(s). Move them to another role first.",
                    role.getName(), members, invites));
        }
        roleRepository.delete(role);
    }

    GarageRoleResponse toResponse(GarageRole role) {
        return GarageRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().isEmpty()
                        ? EnumSet.noneOf(GaragePermission.class) : EnumSet.copyOf(role.getPermissions()))
                .isSystem(role.getIsSystem())
                .memberCount(memberRepository.countByRoleIdAndStatusNot(role.getId(), TeamMemberStatus.REMOVED))
                .pendingInviteCount(requestRepository.countByRoleIdAndApprovalStatus(role.getId(), ApprovalStatus.HOLD))
                .build();
    }

    private GarageRole editableRole(Integer garageId, Integer roleId) {
        GarageRole role = roleRepository.findByIdAndGarageId(roleId, garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found in this garage: " + roleId));
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BadRequestException("The Owner role can't be changed or deleted");
        }
        return role;
    }

    private Garage requireGarage(Integer garageId) {
        return garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
    }

    private void ensureNameFree(Integer garageId, String name, Integer exceptRoleId) {
        if (OWNER.equalsIgnoreCase(name)) {
            throw new BadRequestException("'Owner' is reserved");
        }
        roleRepository.findByGarageIdAndNameIgnoreCase(garageId, name)
                .filter(existing -> !existing.getId().equals(exceptRoleId))
                .ifPresent(existing -> {
                    throw new ConflictException("A role named '" + name + "' already exists");
                });
    }

    private static String validName(String name) {
        String trimmed = trimToNull(name);
        if (trimmed == null) {
            throw new BadRequestException("Role name is required");
        }
        if (trimmed.length() > 60) {
            throw new BadRequestException("Role name must be at most 60 characters");
        }
        return trimmed;
    }

    private static Set<GaragePermission> permissionsOf(GarageRoleRequest request) {
        return request.getPermissions() == null || request.getPermissions().isEmpty()
                ? EnumSet.noneOf(GaragePermission.class)
                : EnumSet.copyOf(request.getPermissions());
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
