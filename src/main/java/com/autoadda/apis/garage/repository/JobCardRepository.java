package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.util.Status;

@Repository
public interface JobCardRepository extends JpaRepository<JobCard, Integer>{

	List<JobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

	List<JobCard> findByTechnicianId(Integer technicianId);

	List<JobCard> findByAppointmentId(Integer appointmentId);

	List<JobCard> findByGarage_Id(Integer garageId);

	List<JobCard> findByGarage_IdAndStatus(Integer garageId, Status status);

}
