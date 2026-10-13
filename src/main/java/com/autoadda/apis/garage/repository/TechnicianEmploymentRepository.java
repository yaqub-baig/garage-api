package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.TechnicianEmployment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicianEmploymentRepository extends TechnicianProfileItemRepository<TechnicianEmployment> {

    /**
     * Counts actual jobs, excluding career breaks (a null entry type is treated as a job).
     */
    @Query("SELECT COUNT(e) FROM TechnicianEmployment e WHERE e.technician.id = :technicianId " +
           "AND (e.entryType IS NULL OR e.entryType <> com.autoadda.apis.garage.util.EmploymentEntryType.CAREER_BREAK)")
    long countJobsByTechnicianId(@Param("technicianId") Integer technicianId);
}
