package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.util.JobCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, Integer> {

    List<JobCard> findByTechnicianIdAndStatus(Integer technicianId, JobCardStatus status);

    List<JobCard> findByTechnicianId(Integer technicianId);

    List<JobCard> findByAppointmentId(Integer appointmentId);

    List<JobCard> findByGarage_Id(Integer garageId);

    List<JobCard> findByGarage_IdAndStatus(Integer garageId, JobCardStatus status);

    @Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceStartTime BETWEEN :start AND :end AND j.garage.id = :garageId")
    long countJobsByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("garageId") Integer garageId);


    // JobCardRepository.java
    @Query("SELECT COUNT(j) FROM JobCard j WHERE j.serviceEndTime BETWEEN :from AND :to")
    int countCompletedJobsBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT j FROM JobCard j WHERE j.technician.id = :id AND j.status = 'COMPLETED'")
    List<JobCard> findJobCardHistoryByTechnicianId(Integer id);
}
