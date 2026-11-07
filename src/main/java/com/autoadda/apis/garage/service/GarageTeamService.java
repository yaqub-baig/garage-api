package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.entity.GarageTeamMember;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.response.MyTeamAccessResponse;
import com.autoadda.apis.garage.response.TeamMemberResponse;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Garage team membership: who is on a garage's team and with which role.
 * <p>
 * The legacy {@code technician_garage} link is kept in sync so existing queries keep working.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageTeamService {

    private final GarageTeamMemberRepository memberRepository;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final GarageRoleService roleService;
    private final GarageAccessGuard guard;

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> listMembers(Integer garageId) {
        Integer ownerId = ownerOf(garageId);
        return memberRepository.findCurrentMembers(garageId).stream()
                .map(m -> toResponse(m, ownerId))
                .toList();
    }

    @Transactional
    public TeamMemberResponse changeRole(Integer garageId, Integer memberId, Integer roleId, Integer actorAutoAddaId) {
        GarageTeamMember member = editableMember(garageId, memberId, actorAutoAddaId);
        GarageRole role = roleService.assignableRole(garageId, roleId);
        roleService.assertCanAssign(actorAutoAddaId, garageId, role.getPermissions());
        member.setRole(role);
        log.info("Garage {}: member {} moved to role '{}' by {}", garageId, memberId, role.getName(), actorAutoAddaId);
        return toResponse(memberRepository.save(member), ownerOf(garageId));
    }

    @Transactional
    public TeamMemberResponse changeStatus(Integer garageId, Integer memberId, TeamMemberStatus status, Integer actorAutoAddaId) {
        if (status != TeamMemberStatus.ACTIVE && status != TeamMemberStatus.SUSPENDED) {
            throw new BadRequestException("Status must be ACTIVE or SUSPENDED. Use DELETE to remove a member.");
        }
        GarageTeamMember member = editableMember(garageId, memberId, actorAutoAddaId);
        member.setStatus(status);
        log.info("Garage {}: member {} set to {} by {}", garageId, memberId, status, actorAutoAddaId);
        return toResponse(memberRepository.save(member), ownerOf(garageId));
    }

    @Transactional
    public void removeMember(Integer garageId, Integer memberId, Integer actorAutoAddaId) {
        GarageTeamMember member = editableMember(garageId, memberId, actorAutoAddaId);
        member.setStatus(TeamMemberStatus.REMOVED);
        memberRepository.save(member);

        Technician technician = member.getTechnician();
        if (technician.getGarages() != null) {
            technician.getGarages().removeIf(g -> g.getId().equals(garageId));
            technicianRepository.save(technician);
        }
        log.info("Garage {}: member {} removed by {}", garageId, memberId, actorAutoAddaId);
    }

    @Transactional(readOnly = true)
    public MyTeamAccessResponse myAccess(Integer garageId, Integer actorAutoAddaId) {
        boolean owner = guard.isOwner(actorAutoAddaId, garageId);
        Optional<GarageTeamMember> member = memberRepository.findByGarageIdAndAutoAddaId(garageId, actorAutoAddaId);
        Set<GaragePermission> permissions = guard.effectivePermissions(actorAutoAddaId, garageId);
        return MyTeamAccessResponse.builder()
                .garageId(garageId)
                .isOwner(owner)
                .isMember(owner || member.map(m -> m.getStatus() != TeamMemberStatus.REMOVED).orElse(false))
                .memberId(member.map(GarageTeamMember::getId).orElse(null))
                .roleId(member.map(m -> m.getRole().getId()).orElse(null))
                .roleName(owner ? GarageRoleService.OWNER : member.map(m -> m.getRole().getName()).orElse(null))
                .status(owner ? TeamMemberStatus.ACTIVE : member.map(GarageTeamMember::getStatus).orElse(null))
                .permissions(permissions)
                .build();
    }

    /**
     * Adds (or re-activates) a person on the garage's team with the given role and links them to the garage.
     * Used when an invite / join request is accepted and when garages are linked directly.
     */
    @Transactional
    public GarageTeamMember addMember(Garage garage, Technician technician, GarageRole role, Integer invitedBy) {
        GarageTeamMember member = memberRepository.findByGarageIdAndTechnicianId(garage.getId(), technician.getId())
                .orElseGet(() -> GarageTeamMember.builder().garage(garage).technician(technician).build());
        member.setRole(role);
        member.setStatus(TeamMemberStatus.ACTIVE);
        member.setInvitedBy(invitedBy);
        GarageTeamMember saved = memberRepository.save(member);

        if (technician.getGarages() == null) {
            technician.setGarages(new HashSet<>());
        }
        if (technician.getGarages().stream().noneMatch(g -> g.getId().equals(garage.getId()))) {
            technician.getGarages().add(garage);
            technicianRepository.save(technician);
        }
        return saved;
    }

    /** Gives the garage owner their Owner membership (called on garage creation and by the backfill). */
    @Transactional
    public void addOwner(Garage garage, Technician ownerTechnician) {
        addMember(garage, ownerTechnician, roleService.ownerRole(garage), null);
    }

    /**
     * Creates team memberships for garages the technician is linked to through the legacy
     * {@code technician_garage} table but has no (active) membership for. They get the default member role,
     * or Owner when they own the garage.
     */
    @Transactional
    public void syncLinkedGarages(Technician technician, Integer invitedBy) {
        if (technician.getGarages() == null) {
            return;
        }
        for (Garage garage : technician.getGarages()) {
            Optional<GarageTeamMember> existing =
                    memberRepository.findByGarageIdAndTechnicianId(garage.getId(), technician.getId());
            if (existing.isPresent() && existing.get().getStatus() != TeamMemberStatus.REMOVED) {
                continue;
            }
            boolean isOwner = technician.getAutoAddaId() != null
                    && technician.getAutoAddaId().equals(garage.getGarageOwner());
            GarageRole role = isOwner ? roleService.ownerRole(garage) : roleService.defaultMemberRole(garage);
            addMember(garage, technician, role, invitedBy);
        }
    }

    private GarageTeamMember editableMember(Integer garageId, Integer memberId, Integer actorAutoAddaId) {
        GarageTeamMember member = memberRepository.findByIdAndGarageId(memberId, garageId)
                .filter(m -> m.getStatus() != TeamMemberStatus.REMOVED)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found: " + memberId));
        Integer memberUserId = member.getTechnician().getAutoAddaId();
        if (memberUserId != null && memberUserId.equals(ownerOf(garageId))) {
            throw new BadRequestException("The garage owner can't be changed or removed");
        }
        if (memberUserId != null && memberUserId.equals(actorAutoAddaId)) {
            throw new BadRequestException("You can't change your own role or access");
        }
        // Only people who hold a member's team-control permissions can change or remove that member.
        roleService.assertCanAssign(actorAutoAddaId, garageId, member.getRole().getPermissions());
        return member;
    }

    private Integer ownerOf(Integer garageId) {
        return garageRepository.findOwnerIdById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
    }

    private static TeamMemberResponse toResponse(GarageTeamMember m, Integer ownerId) {
        Technician t = m.getTechnician();
        return TeamMemberResponse.builder()
                .id(m.getId())
                .technicianId(t.getId())
                .autoAddaId(t.getAutoAddaId())
                .firstName(t.getFirstName())
                .lastName(t.getLastName())
                .email(t.getEmailId())
                .mobileNumber(t.getMobileNumber())
                .profileImg(t.getProfileImg())
                .roleId(m.getRole().getId())
                .roleName(m.getRole().getName())
                .status(m.getStatus())
                .isOwner(t.getAutoAddaId() != null && t.getAutoAddaId().equals(ownerId))
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
