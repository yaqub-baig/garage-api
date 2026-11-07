package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.entity.GarageTeamMember;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageAccessGuardTest {

    private static final int GARAGE = 10;
    private static final int OWNER = 7;
    private static final int STAFF = 42;

    @Mock private GarageRepository garageRepository;
    @Mock private GarageTeamMemberRepository memberRepository;
    @InjectMocks private GarageAccessGuard guard;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(guard, "mode", GarageAccessGuard.Mode.ENFORCE);
        lenient().when(garageRepository.findOwnerIdById(GARAGE)).thenReturn(Optional.of(OWNER));
    }

    private void staffWith(TeamMemberStatus status, GaragePermission... permissions) {
        GarageRole role = GarageRole.builder().id(1).name("Front Desk")
                .permissions(permissions.length == 0 ? EnumSet.noneOf(GaragePermission.class) : EnumSet.of(permissions[0], permissions))
                .build();
        GarageTeamMember member = GarageTeamMember.builder().id(5).role(role).status(status).build();
        when(memberRepository.findByGarageIdAndAutoAddaId(GARAGE, STAFF)).thenReturn(Optional.of(member));
    }

    @Test
    void ownerAlwaysPasses() {
        assertDoesNotThrow(() -> guard.require("7", GARAGE, GaragePermission.ROLE_MANAGE));
        verifyNoInteractions(memberRepository);
    }

    @Test
    void memberWithPermissionPasses() {
        staffWith(TeamMemberStatus.ACTIVE, GaragePermission.APPOINTMENT_MANAGE);
        assertDoesNotThrow(() -> guard.require("42", GARAGE, GaragePermission.APPOINTMENT_MANAGE));
    }

    @Test
    void memberWithoutPermissionIsForbidden() {
        staffWith(TeamMemberStatus.ACTIVE, GaragePermission.APPOINTMENT_MANAGE);
        ForbiddenException e = assertThrows(ForbiddenException.class,
                () -> guard.require("42", GARAGE, GaragePermission.JOB_CARD_ASSIGN));
        assertTrue(e.getMessage().contains("Front Desk"));
    }

    @Test
    void suspendedMemberIsForbidden() {
        staffWith(TeamMemberStatus.SUSPENDED, GaragePermission.APPOINTMENT_MANAGE);
        ForbiddenException e = assertThrows(ForbiddenException.class,
                () -> guard.require("42", GARAGE, GaragePermission.APPOINTMENT_MANAGE));
        assertTrue(e.getMessage().contains("suspended"));
    }

    @Test
    void nonMemberIsForbidden() {
        when(memberRepository.findByGarageIdAndAutoAddaId(GARAGE, STAFF)).thenReturn(Optional.empty());
        assertThrows(ForbiddenException.class, () -> guard.require("42", GARAGE, GaragePermission.TEAM_VIEW));
    }

    @Test
    void missingHeaderIsForbidden() {
        ForbiddenException e = assertThrows(ForbiddenException.class,
                () -> guard.require(null, GARAGE, GaragePermission.TEAM_VIEW));
        assertTrue(e.getMessage().contains("X-Auto-Adda-Id"));
    }

    @Test
    void logOnlyModeDoesNotBlock() {
        ReflectionTestUtils.setField(guard, "mode", GarageAccessGuard.Mode.LOG_ONLY);
        when(memberRepository.findByGarageIdAndAutoAddaId(GARAGE, STAFF)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> guard.require("42", GARAGE, GaragePermission.INVOICE_MANAGE));
    }

    @Test
    void customerPassesCustomerSideCheckWithoutMembership() {
        assertDoesNotThrow(() -> guard.requireUnlessCustomer("99", GARAGE, 99, GaragePermission.INVOICE_MANAGE));
        verifyNoInteractions(memberRepository, garageRepository);
    }

    @Test
    void ownerHasEveryPermission() {
        assertEquals(EnumSet.allOf(GaragePermission.class), guard.effectivePermissions(OWNER, GARAGE));
    }
}
