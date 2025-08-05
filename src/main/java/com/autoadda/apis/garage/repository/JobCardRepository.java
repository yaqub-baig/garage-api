package com.autoadda.apis.garage.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.util.Status;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, Integer> {

	List<JobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

	List<JobCard> findByTechnicianId(Integer technicianId);

	List<JobCard> findByAppointmentId(Integer appointmentId);

	List<JobCard> findByGarage_Id(Integer garageId);

	List<JobCard> findByGarage_IdAndStatus(Integer garageId, Status status);

	@Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceStartTime BETWEEN :start AND :end AND j.garage.id = :garageId")
	long countJobsByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("garageId") Integer garageId);

//	@Query("SELECT j.technician.id, COUNT(j) FROM JobCard j WHERE j.garage.id = :garageId AND j.status = 'COMPLETED' GROUP BY j.technician.id")
//	List<Object[]> countCompletedJobsPerTechnician(@Param("garageId") Integer garageId);

	// JobCardRepository.java
	@Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceEndTime BETWEEN :from AND :to")
	int countCompletedJobsBetween(LocalDateTime from, LocalDateTime to);

//	@Query("SELECT j.technician.name, COUNT(j), SUM(i.totalAmount) FROM JobCard j JOIN Invoice i ON j.id = i.jobCard.id "
//			+ "WHERE j.serviceEndTime BETWEEN :from AND :to GROUP BY j.technician.name")
//	List<Object[]> findTechnicianSummary(LocalDateTime from, LocalDateTime to);

}
