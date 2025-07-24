package com.autoadda.apis.garage.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autoadda.apis.garage.entity.TechnicianAvailability;
import com.autoadda.apis.garage.util.Status;

public interface TechnicianAvailabilityRepository extends JpaRepository<TechnicianAvailability, Integer> {
	List<TechnicianAvailability> findByTechnicianIdAndDate(Integer technicianId, LocalDate date);

	List<TechnicianAvailability> findByTechnicianId(Integer technicianId);

	List<TechnicianAvailability> findByDate(LocalDate date);

	List<TechnicianAvailability> findByTechnicianIdAndStatus(Integer technicianId, Status status);

	List<TechnicianAvailability> findByStatus(String status);

	Optional<TechnicianAvailability> findById(Integer id);

	@Query("SELECT ta.technician.id, SUM(ta.availableHours) FROM GarageTechnicianAvailability ta WHERE ta.technician.garage.id = :garageId GROUP BY ta.technician.id")
	List<Object[]> getAvailableHoursPerTechnician(@Param("garageId") Integer garageId);
}
