package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AppointmentDetailsRepository extends JpaRepository<AppointmentDetails, Integer> {

    List<AppointmentDetails> findByStatus(Status status);

    List<AppointmentDetails> findByCustomerId(Integer customerId);

    List<AppointmentDetails> findByGarage_Id(Integer garageId);

    List<AppointmentDetails> findByGarage_IdAndStatus(Integer garageId, Status status);

    Set<AppointmentDetails> findByGarageIdAndScheduleDate(Integer garageId, LocalDate date);

    Optional<AppointmentDetails> findByGarage_IdAndScheduleDateAndServiceStartTime(Integer garageId,
                                                                                   LocalDate scheduleDate, LocalTime serviceStartTime);

    boolean existsByGarage_IdAndScheduleDateAndServiceStartTimeAndStatus(Integer garageId, LocalDate scheduleDate,
                                                                         LocalTime serviceStartTime, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM AppointmentDetails b WHERE b.garage.id = :garageId AND b.scheduleDate = :date AND b.serviceStartTime = :time AND b.slot.currentBookings < b.slot.maxBookings")
    Optional<AppointmentDetails> findLockedSlot(@Param("garageId") Integer garageId, @Param("date") LocalDate date,
                                                @Param("time") LocalTime time);


    @Query("SELECT a.appointmentReferenceId FROM AppointmentDetails a WHERE a.appointmentReferenceId LIKE :prefix% ORDER BY a.appointmentReferenceId DESC")
    List<String> findLatestCodesByPrefix(@Param("prefix") String codePrefix);

    boolean existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Status status
    );

    Optional<AppointmentDetails> findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Status status
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppointmentDetails a " +
            "WHERE a.vehicleId = :vehicleId " +
            "AND a.scheduleDate = :scheduleDate " +
            "AND a.serviceStartTime = :serviceStartTime " +
            "AND a.status = :status " +
            "AND a.id != :appointmentId")
    boolean existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatusAndIdNot(
            @Param("vehicleId") Integer vehicleId,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("serviceStartTime") LocalTime serviceStartTime,
            @Param("status") Status status,
            @Param("appointmentId") Integer appointmentId
    );
}
