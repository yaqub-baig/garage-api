package com.autoadda.apis.garage.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autoadda.apis.garage.entity.TechnicianShift;

public interface TechnicianShiftRepository extends JpaRepository<TechnicianShift, Integer> {

	@Query("SELECT s FROM TechnicianShift s WHERE s.technician.id = :techId AND "
			+ "(s.specificDate BETWEEN :fromDate AND :toDate OR (s.specificDate IS NULL AND s.active = true))")
	List<TechnicianShift> findShiftsInPeriod(@Param("techId") Integer technicianId,
			@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
