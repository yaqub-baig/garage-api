package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.entity.GarageTeamMember;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.service.GarageRoleService;
import com.autoadda.apis.garage.service.GarageTeamService;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageTeamServiceTest {

    @Mock private GarageTeamMemberRepository memberRepository;
    @Mock private GarageRepository garageRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private GarageRoleService roleService;
    @Mock private GarageAccessGuard guard;
    @InjectMocks private GarageTeamService teamService;

    private final Garage garage = Garage.builder().id(10).garageOwner(7).build();
    private final GarageRole techRole = GarageRole.builder().id(5).name("Technician")
            .permissions(EnumSet.of(GaragePermission.JOB_CARD_UPDATE)).isSystem(false).build();

    @BeforeEach
    void setUp() {
        lenient().when(garageRepository.findOwnerIdById(10)).thenReturn(Optional.of(7));
        lenient().when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private GarageTeamMember member(int id, int autoAddaId) {
        Technician t = Technician.builder().id(100 + id).autoAddaId(autoAddaId)
                .garages(new HashSet<>(Set.of(garage))).build();
        GarageTeamMember m = GarageTeamMember.builder().id(id).garage(garage).technician(t)
                .role(techRole).status(TeamMemberStatus.ACTIVE).build();
        when(memberRepository.findByIdAndGarageId(id, 10)).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void ownerCannotBeChanged() {
        member(1, 7);
        assertThrows(BadRequestException.class, () -> teamService.changeRole(10, 1, 5, 42));
    }

    @Test
    void cannotChangeOwnAccess() {
        member(2, 42);
        assertThrows(BadRequestException.class,
                () -> teamService.changeStatus(10, 2, TeamMemberStatus.SUSPENDED, 42));
    }

    @Test
    void removeMarksRemovedAndUnlinksGarage() {
        GarageTeamMember m = member(3, 55);
        teamService.removeMember(10, 3, 7);

        assertEquals(TeamMemberStatus.REMOVED, m.getStatus());
        assertTrue(m.getTechnician().getGarages().isEmpty());
        verify(technicianRepository).save(m.getTechnician());
    }

    @Test
    void addMemberCreatesMembershipAndLink() {
        Technician t = Technician.builder().id(200).autoAddaId(77).garages(new HashSet<>()).build();
        when(memberRepository.findByGarageIdAndTechnicianId(10, 200)).thenReturn(Optional.empty());

        GarageTeamMember m = teamService.addMember(garage, t, techRole, 7);

        assertEquals(TeamMemberStatus.ACTIVE, m.getStatus());
        assertEquals(techRole, m.getRole());
        assertTrue(t.getGarages().contains(garage));
        verify(technicianRepository).save(t);
    }

    @Test
    void suspendingRequiresStatusActiveOrSuspended() {
        assertThrows(BadRequestException.class,
                () -> teamService.changeStatus(10, 9, TeamMemberStatus.REMOVED, 7));
    }
}
