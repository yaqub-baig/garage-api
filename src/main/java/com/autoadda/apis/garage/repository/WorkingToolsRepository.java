package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.WorkingTools;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingToolsRepository extends JpaRepository<WorkingTools, Integer> {

    // Find all tools for a garage
    List<WorkingTools> findByGarageIdAndIsActiveTrue(Integer garageId);

    // Search by tool name, category or brand
    @Query("SELECT w FROM WorkingTools w WHERE w.garage.id = :garageId " +
           "AND w.isActive = true " +
           "AND (LOWER(w.toolName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(w.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(w.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<WorkingTools> searchWorkingTools(@Param("garageId") Integer garageId,
                                           @Param("searchTerm") String searchTerm);

    // Count of active tools
    Long countByGarageIdAndIsActiveTrue(Integer garageId);
}
