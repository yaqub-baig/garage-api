package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.service.GarageRoleService;
import com.autoadda.apis.garage.service.GarageTeamService;
import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Brings existing data into the team model on startup. Idempotent: it only touches garages without roles
 * and technician-garage links without a team membership, so after the first run it does nothing.
 * <ul>
 *   <li>seeds the default roles for every garage that has none</li>
 *   <li>creates a membership for every {@code technician_garage} link: owner &rarr; Owner,
 *       SUPERVISOR &rarr; Manager, everyone else &rarr; the default member role</li>
 * </ul>
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class GarageTeamBackfillRunner implements ApplicationRunner {

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final GarageRoleService roleService;
    private final GarageTeamService teamService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Integer> garagesWithoutRoles = nativeIds(
                    "SELECT g.id FROM garage g LEFT JOIN garage_team_role r ON r.garage_id = g.id WHERE r.id IS NULL");
            garagesWithoutRoles.forEach(id -> transactionTemplate.executeWithoutResult(s ->
                    garageRepository.findById(id).ifPresent(roleService::seedDefaultRoles)));

            @SuppressWarnings("unchecked")
            List<Object[]> missingLinks = entityManager.createNativeQuery(
                    "SELECT tg.technician_id, tg.garage_id FROM technician_garage tg " +
                    "LEFT JOIN garage_team_member m ON m.garage_id = tg.garage_id AND m.technician_id = tg.technician_id " +
                    "WHERE m.id IS NULL").getResultList();
            missingLinks.forEach(row -> transactionTemplate.executeWithoutResult(s ->
                    addMember(((Number) row[0]).intValue(), ((Number) row[1]).intValue())));

            if (!garagesWithoutRoles.isEmpty() || !missingLinks.isEmpty()) {
                log.info("Team backfill: seeded roles for {} garage(s), created {} membership(s)",
                        garagesWithoutRoles.size(), missingLinks.size());
            }
        } catch (Exception e) {
            // Never block startup; the guard still lets garage owners through.
            log.error("Team backfill failed: {}", e.getMessage(), e);
        }
    }

    private void addMember(Integer technicianId, Integer garageId) {
        Technician technician = technicianRepository.findById(technicianId).orElse(null);
        Garage garage = garageRepository.findById(garageId).orElse(null);
        if (technician == null || garage == null || Boolean.TRUE.equals(technician.getIsDeleted())) {
            return;
        }
        boolean owner = technician.getAutoAddaId() != null && technician.getAutoAddaId().equals(garage.getGarageOwner());
        if (owner) {
            teamService.addOwner(garage, technician);
        } else if (technician.getEmployeeRole() == EMPLOYEE_ROLE.SUPERVISOR) {
            teamService.addMember(garage, technician, roleService.roleNamed(garage, GarageRoleService.MANAGER), null);
        } else {
            teamService.addMember(garage, technician, roleService.defaultMemberRole(garage), null);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Integer> nativeIds(String sql) {
        return ((List<Number>) entityManager.createNativeQuery(sql).getResultList()).stream()
                .map(Number::intValue).toList();
    }
}
