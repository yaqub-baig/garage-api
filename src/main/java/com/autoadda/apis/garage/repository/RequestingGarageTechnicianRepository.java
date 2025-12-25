package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.RequestingGarageAndTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestingGarageTechnicianRepository extends JpaRepository<RequestingGarageAndTechnician, Integer> {

    /**
     * Find all pending requests where the garage is the recipient (sent by technician)
     * Used for: Garage dashboard showing requests from technicians awaiting approval
     */
    @Query("SELECT DISTINCT r FROM RequestingGarageAndTechnician r " +
           "LEFT JOIN FETCH r.technician t " +
           "LEFT JOIN FETCH r.garage g " +
           "WHERE r.garage.id = :garageId " +
           "AND r.sendBy = 'TECHNICIAN' " +
           "AND r.approvalStatus = 'HOLD' " +
           "ORDER BY r.requestedAt DESC")
    List<RequestingGarageAndTechnician> findPendingRequestsForGarage(@Param("garageId") Integer garageId);

    /**
     * Find all pending requests where the technician is the recipient (sent by garage)
     * Used for: Technician dashboard showing requests from garages awaiting approval
     */
    @Query("SELECT DISTINCT r FROM RequestingGarageAndTechnician r " +
           "LEFT JOIN FETCH r.technician t " +
           "LEFT JOIN FETCH r.garage g " +
           "WHERE r.technician.id = :technicianId " +
           "AND r.sendBy = 'GARAGE' " +
           "AND r.approvalStatus = 'HOLD' " +
           "ORDER BY r.requestedAt DESC")
    List<RequestingGarageAndTechnician> findPendingRequestsForTechnician(@Param("technicianId") Integer technicianId);

    /**
     * Find all requests involving a garage (sent or received, all statuses)
     * Used for: Audit trail and request history for garage
     */
    @Query("SELECT DISTINCT r FROM RequestingGarageAndTechnician r " +
           "LEFT JOIN FETCH r.technician t " +
           "LEFT JOIN FETCH r.garage g " +
           "WHERE r.garage.id = :garageId " +
           "ORDER BY r.requestedAt DESC")
    List<RequestingGarageAndTechnician> findAllRequestsByGarage(@Param("garageId") Integer garageId);

    /**
     * Find all requests involving a technician (sent or received, all statuses)
     * Used for: Audit trail and request history for technician
     */
    @Query("SELECT DISTINCT r FROM RequestingGarageAndTechnician r " +
           "LEFT JOIN FETCH r.technician t " +
           "LEFT JOIN FETCH r.garage g " +
           "WHERE r.technician.id = :technicianId " +
           "ORDER BY r.requestedAt DESC")
    List<RequestingGarageAndTechnician> findAllRequestsByTechnician(@Param("technicianId") Integer technicianId);

    /**
     * Find existing request between specific garage and technician with HOLD status
     * Used for: Preventing duplicate pending requests
     */
    @Query("SELECT r FROM RequestingGarageAndTechnician r " +
           "WHERE r.garage.id = :garageId " +
           "AND r.technician.id = :technicianId " +
           "AND r.approvalStatus = 'HOLD'")
    Optional<RequestingGarageAndTechnician> findHoldRequestBetween(
            @Param("garageId") Integer garageId,
            @Param("technicianId") Integer technicianId);

    /**
     * Check if active HOLD request exists (returns boolean directly)
     * Used for: Quick validation without fetching the entity
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM RequestingGarageAndTechnician r " +
           "WHERE r.garage.id = :garageId " +
           "AND r.technician.id = :technicianId " +
           "AND r.approvalStatus = 'HOLD'")
    boolean existsHoldRequestBetween(
            @Param("garageId") Integer garageId,
            @Param("technicianId") Integer technicianId);

    /**
     * Find request by ID with eager loading to avoid N+1 queries
     * Used for: Approve/Reject operations needing full entity graph
     */
    @Query("SELECT DISTINCT r FROM RequestingGarageAndTechnician r " +
           "LEFT JOIN FETCH r.technician t " +
           "LEFT JOIN FETCH r.garage g " +
           "WHERE r.id = :id")
    Optional<RequestingGarageAndTechnician> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Count pending requests for a garage
     */
    @Query("SELECT COUNT(r) FROM RequestingGarageAndTechnician r " +
           "WHERE r.garage.id = :garageId " +
           "AND r.sendBy = 'TECHNICIAN' " +
           "AND r.approvalStatus = 'HOLD'")
    long countPendingRequestsForGarage(@Param("garageId") Integer garageId);

    /**
     * Count pending requests for a technician
     */
    @Query("SELECT COUNT(r) FROM RequestingGarageAndTechnician r " +
           "WHERE r.technician.id = :technicianId " +
           "AND r.sendBy = 'GARAGE' " +
           "AND r.approvalStatus = 'HOLD'")
    long countPendingRequestsForTechnician(@Param("technicianId") Integer technicianId);
}
