package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsAppointmentDetails;
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
public interface OsAppointmentDetailsRepository extends JpaRepository<OsAppointmentDetails, Integer> {



    boolean existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Status status
    );

    Optional<OsAppointmentDetails> findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Status status
    );

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM OsAppointmentDetails a " +
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
