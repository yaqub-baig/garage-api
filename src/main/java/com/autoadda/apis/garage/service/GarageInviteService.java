package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRole;
import com.autoadda.apis.garage.entity.RequestingGarageAndTechnician;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.repository.RequestingGarageTechnicianRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.request.TeamInviteRequest;
import com.autoadda.apis.garage.response.TeamInviteResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

/**
 * Inviting people to a garage's team by phone number.
 * <p>
 * Invites are stored as garage-sent {@link RequestingGarageAndTechnician} requests carrying a role, so they are
 * accepted / rejected through the existing {@code /requestingGarageTechnician/technician/{id}/approve|reject}.
 * If the phone number has no AutoAdda account yet, the invite is kept against the phone number and the
 * person is linked to it the first time they open their invites after signing up.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageInviteService {

    private final RequestingGarageTechnicianRepository requestRepository;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final GarageTeamMemberRepository memberRepository;
    private final GarageRoleService roleService;
    private final AuthClient authClient;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationMessageBuilder notificationMessageBuilder;

    @Transactional
    public TeamInviteResponse invite(Integer garageId, TeamInviteRequest request, Integer actorAutoAddaId) {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
        String phone = normalizePhone(request.getPhone());
        GarageRole role = roleService.assignableRole(garageId, request.getRoleId());
        roleService.assertCanAssign(actorAutoAddaId, garageId, role.getPermissions());

        UserResponse user = findUserByPhone(phone);
        RequestingGarageAndTechnician invite = new RequestingGarageAndTechnician();
        invite.setGarage(garage);
        invite.setSendBy(SendBy.GARAGE);
        invite.setApprovalStatus(ApprovalStatus.HOLD);
        invite.setDescription(trimToNull(request.getMessage()));
        invite.setRole(role);
        invite.setInvitedPhone(phone);
        invite.setInvitedBy(actorAutoAddaId);

        if (user != null) {
            Technician technician = findOrCreateTechnician(user);
            memberRepository.findByGarageIdAndTechnicianId(garageId, technician.getId())
                    .filter(m -> m.getStatus() != TeamMemberStatus.REMOVED)
                    .ifPresent(m -> {
                        throw new AlreadyExistsException("This person is already on the team");
                    });
            if (requestRepository.existsHoldRequestBetween(garageId, technician.getId())) {
                throw new ConflictException("A pending invite or join request already exists for this person");
            }
            invite.setTechnician(technician);
            RequestingGarageAndTechnician saved = requestRepository.save(invite);
            notifyExistingUser(garage, role, technician, invite.getDescription());
            log.info("Garage {}: invited user {} as '{}' (invite {})", garageId, user.getId(), role.getName(), saved.getId());
            return toResponse(saved);
        }

        if (requestRepository.existsPendingPhoneInvite(garageId, phone)) {
            throw new ConflictException("A pending invite already exists for this phone number");
        }
        RequestingGarageAndTechnician saved = requestRepository.save(invite);
        smsSignUpInvite(garage, role, phone);
        log.info("Garage {}: invited unregistered phone as '{}' (invite {})", garageId, role.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TeamInviteResponse> pendingInvites(Integer garageId) {
        return requestRepository.findPendingInvitesByGarage(garageId).stream().map(GarageInviteService::toResponse).toList();
    }

    @Transactional
    public void cancelInvite(Integer garageId, Integer inviteId) {
        RequestingGarageAndTechnician invite = requestRepository.findById(inviteId)
                .filter(r -> r.getGarage().getId().equals(garageId) && r.getSendBy() == SendBy.GARAGE)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found: " + inviteId));
        if (invite.getApprovalStatus() != ApprovalStatus.HOLD) {
            throw new BadRequestException("Only pending invites can be cancelled");
        }
        invite.setApprovalStatus(ApprovalStatus.CANCELLED);
        requestRepository.save(invite);
    }

    /**
     * Pending invites for the calling user. Invites sent to their phone number before they had an
     * account are linked to them here.
     */
    @Transactional
    public List<TeamInviteResponse> myInvites(Integer autoAddaId) {
        UserResponse user = authClient.getUser(autoAddaId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + autoAddaId);
        }
        Technician technician = technicianRepository.findByAutoAddaId(autoAddaId).orElse(null);

        String phone = user.getMobileNumber() != null ? normalizePhone(user.getMobileNumber()) : null;
        if (phone != null) {
            List<RequestingGarageAndTechnician> unclaimed = requestRepository.findUnclaimedInvitesByPhone(phone);
            if (!unclaimed.isEmpty()) {
                if (technician == null) {
                    technician = findOrCreateTechnician(user);
                }
                for (RequestingGarageAndTechnician invite : unclaimed) {
                    invite.setTechnician(technician);
                    requestRepository.save(invite);
                }
                log.info("Linked {} phone invite(s) to user {}", unclaimed.size(), autoAddaId);
            }
        }
        if (technician == null) {
            return List.of();
        }
        return requestRepository.findPendingRequestsForTechnician(technician.getId()).stream()
                .map(GarageInviteService::toResponse)
                .toList();
    }

    /** Existing (non-deleted) technician record for this user, or a new one created from their account. */
    private Technician findOrCreateTechnician(UserResponse user) {
        return technicianRepository.findByAutoAddaId(user.getId())
                .map(existing -> {
                    if (Boolean.TRUE.equals(existing.getIsDeleted())) {
                        existing.setIsDeleted(false);
                        return technicianRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> technicianRepository.save(Technician.builder()
                        .autoAddaId(user.getId())
                        .firstName(user.getName())
                        .emailId(user.getEmail())
                        .mobileNumber(user.getMobileNumber())
                        .status(Status.ACTIVE)
                        .employeeRole(EMPLOYEE_ROLE.TECHNICIAN)
                        .garages(new HashSet<>())
                        .build()));
    }

    private UserResponse findUserByPhone(String phone) {
        try {
            ResponseEntity<UserResponse> response = authClient.getUserDetails(phone);
            return response != null && response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
        } catch (Exception e) {
            // The auth service answers 404 for unknown numbers.
            log.info("No AutoAdda account for invited phone ({})", e.getClass().getSimpleName());
            return null;
        }
    }

    private void notifyExistingUser(Garage garage, GarageRole role, Technician technician, String message) {
        try {
            String description = "You've been invited to join the team as " + role.getName()
                    + (message != null ? ". " + message : "");
            NotificationContent content = notificationMessageBuilder
                    .buildCollaborationRequestReceived(garage.getName(), "Garage", description);
            notificationDispatchService.sendNotification(technician.getAutoAddaId(),
                    NotificationType.COLLABORATION_REQUESTS, content, false,
                    RequestingGarageTechnicianService.GARAGE_TECH);
        } catch (Exception e) {
            log.error("Failed to send team invite notification: {}", e.getMessage(), e);
        }
    }

    private void smsSignUpInvite(Garage garage, GarageRole role, String phone) {
        try {
            authClient.sendSms(new SmsRequest(phone, String.format(
                    "%s has invited you to join their team on AutoAdda as %s. "
                            + "Sign up at https://autoadda.ae with this number to accept.",
                    garage.getName(), role.getName())));
        } catch (Exception e) {
            log.error("Failed to send team invite SMS: {}", e.getMessage(), e);
        }
    }

    /** Keeps a leading '+' and digits only, so "+971 50-123 4567" and "+971501234567" match. */
    static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("Phone number is required");
        }
        String trimmed = phone.trim();
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.length() < 7) {
            throw new BadRequestException("Invalid phone number: " + phone);
        }
        return (trimmed.startsWith("+") ? "+" : "") + digits;
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static TeamInviteResponse toResponse(RequestingGarageAndTechnician r) {
        Technician t = r.getTechnician();
        return TeamInviteResponse.builder()
                .id(r.getId())
                .garageId(r.getGarage().getId())
                .garageName(r.getGarage().getName())
                .phone(r.getInvitedPhone() != null ? r.getInvitedPhone() : t != null ? t.getMobileNumber() : null)
                .technicianId(t != null ? t.getId() : null)
                .inviteeName(t != null ? t.getFirstName() : null)
                .roleId(r.getRole() != null ? r.getRole().getId() : null)
                .roleName(r.getRole() != null ? r.getRole().getName() : null)
                .message(r.getDescription())
                .sendBy(r.getSendBy())
                .status(r.getApprovalStatus())
                .invitedBy(r.getInvitedBy())
                .invitedAt(r.getRequestedAt())
                .build();
    }
}
