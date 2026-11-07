package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageTeamMember;
import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.util.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarageTeamMemberRepository extends JpaRepository<GarageTeamMember, Integer> {

    @Query("SELECT m FROM GarageTeamMember m JOIN FETCH m.technician JOIN FETCH m.role " +
           "WHERE m.garage.id = :garageId AND m.status <> 'REMOVED' ORDER BY m.joinedAt ASC")
    List<GarageTeamMember> findCurrentMembers(@Param("garageId") Integer garageId);

    Optional<GarageTeamMember> findByGarageIdAndTechnicianId(Integer garageId, Integer technicianId);

    @Query("SELECT m FROM GarageTeamMember m JOIN FETCH m.role " +
           "WHERE m.garage.id = :garageId AND m.technician.autoAddaId = :autoAddaId " +
           "AND (m.technician.isDeleted IS NULL OR m.technician.isDeleted = false)")
    Optional<GarageTeamMember> findByGarageIdAndAutoAddaId(@Param("garageId") Integer garageId,
                                                           @Param("autoAddaId") Integer autoAddaId);

    Optional<GarageTeamMember> findByIdAndGarageId(Integer id, Integer garageId);

    long countByRoleIdAndStatusNot(Integer roleId, TeamMemberStatus status);

    @Query("SELECT COUNT(m) FROM GarageTeamMember m JOIN m.role r JOIN r.permissions p " +
           "WHERE m.garage.id = :garageId AND m.status = 'ACTIVE' AND p = :permission")
    long countActiveWithPermission(@Param("garageId") Integer garageId,
                                   @Param("permission") GaragePermission permission);
}
