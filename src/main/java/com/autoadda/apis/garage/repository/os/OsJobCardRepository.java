package com.autoadda.apis.garage.repository.os;

import java.time.LocalDateTime;
import java.util.List;

import com.autoadda.apis.garage.entity.os.OsJobCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.util.Status;

@Repository
public interface OsJobCardRepository extends JpaRepository<OsJobCard, Integer> {

    List<OsJobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

    List<OsJobCard> findByTechnicianId(Integer technicianId);

    List<OsJobCard> findByAppointmentId(Integer osAppointmentId);

    List<OsJobCard> findByOtherServiceId(Integer otherServiceId);

    List<OsJobCard> findByGarage_IdAndStatus(Integer otherServiceId, Status status);

    @Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceStartTime BETWEEN :start AND :end AND j.otherService.id = :otherServiceId")
    long countJobsByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("otherServiceId") Integer garageId);

//	@Query("SELECT j.technician.id, COUNT(j) FROM JobCard j WHERE j.garage.id = :garageId AND j.status = 'COMPLETED' GROUP BY j.technician.id")
//	List<Object[]> countCompletedJobsPerTechnician(@Param("garageId") Integer garageId);

    // JobCardRepository.java
    @Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceEndTime BETWEEN :from AND :to")
    int countCompletedJobsBetween(LocalDateTime from, LocalDateTime to);

//	@Query("SELECT j.technician.name, COUNT(j), SUM(i.totalAmount) FROM JobCard j JOIN Invoice i ON j.id = i.jobCard.id "
//			+ "WHERE j.serviceEndTime BETWEEN :from AND :to GROUP BY j.technician.name")
//	List<Object[]> findTechnicianSummary(LocalDateTime from, LocalDateTime to);

}
