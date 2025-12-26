package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.Technician;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

    Optional<Technician> findByEmailId(String email);

    Optional<Technician> findByMobileNumber(String phone);

    @Query("SELECT t FROM Technician t JOIN t.skillSet s WHERE s.id = :skillId")
    List<Technician> findBySkillSetId(@Param("skillId") Integer skillId);

    List<Technician> findByStatus(String status);

    Optional<Technician> findByAutoAddaId(Integer autoAddaId);

    /**
     * Fetch all active (non-deleted) technicians with eager loading of relationships
     * to avoid N+1 query problems during search operations.
     * Uses LEFT JOIN FETCH for garages, skillSet, and expertise associations.
     */
    @Query("SELECT DISTINCT t FROM Technician t " +
           "LEFT JOIN FETCH t.garages " +
           "LEFT JOIN FETCH t.skillSet " +
           "LEFT JOIN FETCH t.expertise " +
           "WHERE t.isDeleted = false AND t.status = 'ACTIVE'")
    Set<Technician> findAllActiveWithEagerLoading();

    /**
     * Paginated query for active technicians (used when no filters applied)
     * Enables database-level pagination for better performance.
     */
    @Query("SELECT DISTINCT t FROM Technician t " +
           "LEFT JOIN FETCH t.garages " +
           "LEFT JOIN FETCH t.skillSet " +
           "LEFT JOIN FETCH t.expertise " +
           "WHERE t.isDeleted = false AND t.status = 'ACTIVE'")
    Page<Technician> findAllActiveWithPagination(Pageable pageable);

    /**
     * Count query for pagination support
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Technician t WHERE t.isDeleted = false AND t.status = 'ACTIVE'")
    long countAllActive();

    /**
     * Find technicians by garage ID (updated for many-to-many)
     * Replaces the old findByGarageId method
     */
    @Query("SELECT DISTINCT t FROM Technician t JOIN t.garages g WHERE g.id = :garageId AND t.isDeleted = false")
    List<Technician> findByGaragesContaining(@Param("garageId") Integer garageId);
}
