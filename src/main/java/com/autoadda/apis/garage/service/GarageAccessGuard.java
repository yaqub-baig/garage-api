package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.GarageTeamMember;
import com.autoadda.apis.garage.exception.ForbiddenException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageTeamMemberRepository;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Checks that the calling user (the {@code X-Auto-Adda-Id} header) holds a permission in a garage.
 * <p>
 * The garage owner always passes. Anyone else needs an ACTIVE team membership whose role grants
 * the permission. Set {@code garage.permissions.enforcement=LOG_ONLY} to log denials without
 * blocking the request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageAccessGuard {

    public enum Mode { ENFORCE, LOG_ONLY }

    private final GarageRepository garageRepository;
    private final GarageTeamMemberRepository memberRepository;

    @Value("${garage.permissions.enforcement:ENFORCE}")
    private Mode mode;

    /** Throws {@link ForbiddenException} (403) unless the caller has {@code permission} in {@code garageId}. */
    @Transactional(readOnly = true)
    public void require(String autoAddaIdHeader, Integer garageId, GaragePermission permission) {
        String denial = check(autoAddaIdHeader, garageId, permission);
        if (denial == null) {
            return;
        }
        if (mode == Mode.LOG_ONLY) {
            log.warn("[permissions LOG_ONLY] would deny {} on garage {} for user {}: {}",
                    permission, garageId, autoAddaIdHeader, denial);
            return;
        }
        throw new ForbiddenException(denial);
    }

    /**
     * Like {@link #require}, but lets the record's own customer through (customer-side actions such as
     * negotiating a work order or paying an invoice).
     */
    @Transactional(readOnly = true)
    public void requireUnlessCustomer(String autoAddaIdHeader, Integer garageId, Integer customerId, GaragePermission permission) {
        if (customerId != null && autoAddaIdHeader != null && autoAddaIdHeader.trim().equals(String.valueOf(customerId))) {
            return;
        }
        require(autoAddaIdHeader, garageId, permission);
    }

    /** Effective permissions of a user in a garage (all of them for the owner, empty if not an active member). */
    @Transactional(readOnly = true)
    public Set<GaragePermission> effectivePermissions(Integer autoAddaId, Integer garageId) {
        if (isOwner(autoAddaId, garageId)) {
            return EnumSet.allOf(GaragePermission.class);
        }
        return memberRepository.findByGarageIdAndAutoAddaId(garageId, autoAddaId)
                .filter(m -> m.getStatus() == TeamMemberStatus.ACTIVE)
                .map(m -> m.getRole().getPermissions().isEmpty()
                        ? EnumSet.noneOf(GaragePermission.class)
                        : EnumSet.copyOf(m.getRole().getPermissions()))
                .orElseGet(() -> EnumSet.noneOf(GaragePermission.class));
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Integer autoAddaId, Integer garageId) {
        Integer ownerId = garageRepository.findOwnerIdById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
        return autoAddaId != null && autoAddaId.equals(ownerId);
    }

    /** Parses the {@code X-Auto-Adda-Id} header, or throws 403 when it is missing or malformed. */
    public static Integer parseCaller(String autoAddaIdHeader) {
        if (autoAddaIdHeader == null || autoAddaIdHeader.isBlank()) {
            throw new ForbiddenException("Missing X-Auto-Adda-Id header");
        }
        try {
            return Integer.valueOf(autoAddaIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new ForbiddenException("Invalid X-Auto-Adda-Id header");
        }
    }

    /** @return null when allowed, otherwise the reason for denial */
    private String check(String autoAddaIdHeader, Integer garageId, GaragePermission permission) {
        Integer caller;
        try {
            caller = parseCaller(autoAddaIdHeader);
        } catch (ForbiddenException e) {
            return e.getMessage();
        }
        if (isOwner(caller, garageId)) {
            return null;
        }
        Optional<GarageTeamMember> member = memberRepository.findByGarageIdAndAutoAddaId(garageId, caller);
        if (member.isEmpty() || member.get().getStatus() == TeamMemberStatus.REMOVED) {
            return "You are not a member of this garage's team";
        }
        if (member.get().getStatus() == TeamMemberStatus.SUSPENDED) {
            return "Your access to this garage is suspended";
        }
        if (!member.get().getRole().getPermissions().contains(permission)) {
            return "Your role '" + member.get().getRole().getName() + "' does not allow: "
                    + permission.getDescription().toLowerCase();
        }
        return null;
    }
}
