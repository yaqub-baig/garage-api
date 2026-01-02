package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.TechnicianExpertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianExpertiseRepository extends JpaRepository<TechnicianExpertise, Integer> {

    /**
     * Delete all expertise records for a specific technician.
     * Used when updating technician expertise to avoid unique constraint violations.
     *
     * @param technicianId The technician ID
     */
    @Modifying
    @Query("DELETE FROM TechnicianExpertise te WHERE te.technician.id = :technicianId")
    void deleteByTechnicianId(@Param("technicianId") Integer technicianId);
}
