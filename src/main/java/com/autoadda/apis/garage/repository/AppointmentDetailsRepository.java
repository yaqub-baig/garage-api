package com.autoadda.apis.garage.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.LockModeType;

@Repository
public interface AppointmentDetailsRepository extends JpaRepository<AppointmentDetails, Integer> {

	List<AppointmentDetails> findByCustomer_Id(Integer customerId);

	List<AppointmentDetails> findByCustomer_IdAndStatus(Integer customerId, Status status);

	List<AppointmentDetails> findByGarage_Id(Integer garageId);

	List<AppointmentDetails> findByGarage_IdAndStatus(Integer garageId, Status status);

	Optional<AppointmentDetails> findByGarage_IdAndScheduleDate(Integer garageId, LocalDate date);

	Optional<AppointmentDetails> findByGarage_IdAndScheduleDateAndServiceStartTime(Integer garageId,
			LocalDate scheduleDate, LocalTime serviceStartTime);

	boolean existsByCustomer_IdAndGarage_IdAndScheduleDateAndServiceStartTimeAndStatus(Integer customerId,
			Integer garageId, LocalDate scheduleDate, LocalTime serviceStartTime, Status status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM AppointmentDetails b WHERE b.garage.id = :garageId AND b.scheduleDate = :date AND b.serviceStartTime = :time")
	Optional<AppointmentDetails> findLockedSlot(@Param("garageId") Integer garageId, @Param("date") LocalDate date,
			@Param("time") LocalTime time);
}
