package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageRoleRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.RequestingGarageTechnicianRepository;
import com.autoadda.apis.garage.request.GarageRoleRequest;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.service.GarageRoleService;
import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageRoleServiceTest {

    @Mock private GarageRoleRepository roleRepository;
    @Mock private GarageRepository garageRepository;
    @Mock private GarageTeamMemberRepository memberRepository;
    @Mock private RequestingGarageTechnicianRepository requestRepository;
    @Mock private GarageAccessGuard guard;
    @InjectMocks private GarageRoleService roleService;

    private final Garage garage = Garage.builder().id(10).build();

    @Test
    void seedsFiveDefaultRolesWithSystemOwner() {
        when(roleRepository.existsByGarageId(10)).thenReturn(false);
        roleService.seedDefaultRoles(garage);

        ArgumentCaptor<GarageRole> saved = ArgumentCaptor.forClass(GarageRole.class);
        verify(roleRepository, times(5)).save(saved.capture());
        List<GarageRole> roles = saved.getAllValues();
        assertEquals(List.of("Owner", "Admin", "Manager", "HR", "Technician"), roles.stream().map(GarageRole::getName).toList());
        assertTrue(roles.get(0).getIsSystem());
        assertEquals(EnumSet.allOf(GaragePermission.class), roles.get(0).getPermissions());
        assertFalse(roles.get(1).getPermissions().contains(GaragePermission.ROLE_MANAGE));
        assertTrue(roles.stream().skip(1).noneMatch(GarageRole::getIsSystem));
    }

    @Test
    void seedingIsSkippedWhenRolesExist() {
        when(roleRepository.existsByGarageId(10)).thenReturn(true);
        roleService.seedDefaultRoles(garage);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void ownerRoleCannotBeEdited() {
        when(roleRepository.findByIdAndGarageId(1, 10)).thenReturn(Optional.of(
                GarageRole.builder().id(1).name("Owner").isSystem(true).build()));
        assertThrows(BadRequestException.class,
                () -> roleService.updateRole(10, 1, new GarageRoleRequest("Boss", null, null), 7));
    }

    @Test
    void roleInUseCannotBeDeleted() {
        when(roleRepository.findByIdAndGarageId(3, 10)).thenReturn(Optional.of(
                GarageRole.builder().id(3).name("Manager").isSystem(false).build()));
        when(memberRepository.countByRoleIdAndStatusNot(3, TeamMemberStatus.REMOVED)).thenReturn(0L);
        when(requestRepository.countByRoleIdAndApprovalStatus(3, ApprovalStatus.HOLD)).thenReturn(1L);
        assertThrows(ConflictException.class, () -> roleService.deleteRole(10, 3));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void cannotCreateRoleWithPermissionsYouLack() {
        when(garageRepository.findById(10)).thenReturn(Optional.of(garage));
        when(roleRepository.existsByGarageId(10)).thenReturn(true);
        when(roleRepository.findByGarageIdAndNameIgnoreCase(10, "Front Desk")).thenReturn(Optional.empty());
        when(guard.effectivePermissions(42, 10)).thenReturn(EnumSet.of(GaragePermission.TEAM_VIEW, GaragePermission.ROLE_MANAGE));

        GarageRoleRequest request = new GarageRoleRequest("Front Desk", null, Set.of(GaragePermission.INVOICE_MANAGE));
        assertThrows(ForbiddenException.class, () -> roleService.createRole(10, request, 42));
    }

    @Test
    void teamManagerCanAssignOperationalRoles() {
        assertDoesNotThrow(() -> roleService.assertCanAssign(62, 10,
                EnumSet.of(GaragePermission.INVOICE_MANAGE, GaragePermission.JOB_CARD_ASSIGN)));
        verifyNoInteractions(guard);
    }

    @Test
    void cannotAssignRoleEditingWithoutHoldingIt() {
        when(guard.effectivePermissions(62, 10)).thenReturn(EnumSet.of(GaragePermission.TEAM_VIEW, GaragePermission.TEAM_MANAGE));
        assertThrows(ForbiddenException.class, () -> roleService.assertCanAssign(62, 10,
                EnumSet.of(GaragePermission.ROLE_MANAGE, GaragePermission.INVOICE_MANAGE)));
    }

    @Test
    void ownerNameIsReserved() {
        when(garageRepository.findById(10)).thenReturn(Optional.of(garage));
        when(roleRepository.existsByGarageId(10)).thenReturn(true);
        assertThrows(BadRequestException.class,
                () -> roleService.createRole(10, new GarageRoleRequest("owner", null, Set.of()), 7));
    }
}
