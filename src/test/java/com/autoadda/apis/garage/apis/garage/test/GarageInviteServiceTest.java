package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.entity.RequestingGarageAndTechnician;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.RequestingGarageTechnicianRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.request.TeamInviteRequest;
import com.autoadda.apis.garage.response.TeamInviteResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.service.*;
import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.SendBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageInviteServiceTest {

    @Mock private RequestingGarageTechnicianRepository requestRepository;
    @Mock private GarageRepository garageRepository;
    @Mock private TechnicianRepository technicianRepository;
    @Mock private GarageTeamMemberRepository memberRepository;
    @Mock private GarageRoleService roleService;
    @Mock private AuthClient authClient;
    @Mock private NotificationDispatchService notificationDispatchService;
    @Mock private NotificationMessageBuilder notificationMessageBuilder;
    @InjectMocks private GarageInviteService inviteService;

    private final Garage garage = Garage.builder().id(10).name("Speedy Motors").garageOwner(7).build();
    private final GarageRole manager = GarageRole.builder().id(3).name("Manager")
            .permissions(EnumSet.of(GaragePermission.WORK_ORDER_MANAGE)).isSystem(false).build();

    @BeforeEach
    void setUp() {
        when(garageRepository.findById(10)).thenReturn(Optional.of(garage));
        when(roleService.assignableRole(10, 3)).thenReturn(manager);
        when(requestRepository.save(any())).thenAnswer(i -> {
            RequestingGarageAndTechnician r = i.getArgument(0);
            r.setId(900);
            return r;
        });
    }

    @Test
    void invitesExistingUserAndNotifiesThem() {
        UserResponse user = new UserResponse();
        user.setId(55);
        user.setName("Sara");
        user.setMobileNumber("+971501234567");
        when(authClient.getUserDetails("+971501234567")).thenReturn(ResponseEntity.ok(user));
        Technician tech = Technician.builder().id(300).autoAddaId(55).firstName("Sara").build();
        when(technicianRepository.findByAutoAddaId(55)).thenReturn(Optional.of(tech));
        when(memberRepository.findByGarageIdAndTechnicianId(10, 300)).thenReturn(Optional.empty());

        TeamInviteResponse response = inviteService.invite(10,
                new TeamInviteRequest("+971 50-123 4567", 3, "Welcome"), 7);

        assertEquals(300, response.getTechnicianId());
        assertEquals("Manager", response.getRoleName());
        assertEquals(SendBy.GARAGE, response.getSendBy());
        assertEquals(ApprovalStatus.HOLD, response.getStatus());
        verify(roleService).assertCanAssign(7, 10, manager.getPermissions());
        verify(notificationDispatchService).sendNotification(eq(55), any(), any(), eq(false), any());
        verify(authClient, never()).sendSms(any());
    }

    @Test
    void invitesUnknownPhoneBySms() {
        when(authClient.getUserDetails("+971509999999")).thenThrow(new RuntimeException("404"));

        TeamInviteResponse response = inviteService.invite(10, new TeamInviteRequest("+971509999999", 3, null), 7);

        assertNull(response.getTechnicianId());
        assertEquals("+971509999999", response.getPhone());
        ArgumentCaptor<SmsRequest> sms = ArgumentCaptor.forClass(SmsRequest.class);
        verify(authClient).sendSms(sms.capture());
        assertTrue(sms.getValue().getMessage().contains("Speedy Motors"));
        assertTrue(sms.getValue().getMessage().contains("Manager"));
    }

    @Test
    void phoneInvitesAreClaimedAfterSignUp() {
        reset(garageRepository, roleService, requestRepository);
        UserResponse user = new UserResponse();
        user.setId(77);
        user.setName("Ali");
        user.setMobileNumber("+971509999999");
        when(authClient.getUser(77)).thenReturn(user);
        when(technicianRepository.findByAutoAddaId(77)).thenReturn(Optional.empty());
        when(technicianRepository.save(any())).thenAnswer(i -> {
            Technician t = i.getArgument(0);
            t.setId(400);
            return t;
        });
        RequestingGarageAndTechnician invite = new RequestingGarageAndTechnician();
        invite.setId(901);
        invite.setGarage(garage);
        invite.setRole(manager);
        invite.setSendBy(SendBy.GARAGE);
        invite.setApprovalStatus(ApprovalStatus.HOLD);
        invite.setInvitedPhone("+971509999999");
        when(requestRepository.findUnclaimedInvitesByPhone("+971509999999")).thenReturn(List.of(invite));
        when(requestRepository.findPendingRequestsForTechnician(400)).thenReturn(List.of(invite));

        List<TeamInviteResponse> mine = inviteService.myInvites(77);

        assertEquals(1, mine.size());
        assertEquals(400, invite.getTechnician().getId());
        assertEquals("Manager", mine.get(0).getRoleName());
    }
}
