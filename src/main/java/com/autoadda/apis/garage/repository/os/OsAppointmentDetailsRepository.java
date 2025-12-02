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

    List<OsAppointmentDetails> findByStatus(Status status);

    List<OsAppointmentDetails> findByCustomerId(Integer customerId);

    List<OsAppointmentDetails> findByOtherServiceId(Integer otherServiceId);

    List<OsAppointmentDetails> findByOtherServiceIdAndStatus(Integer otherServiceId, Status status);

    Set<OsAppointmentDetails> findByOtherServiceIdAndScheduleDate(Integer otherServiceId, LocalDate date);

    Optional<OsAppointmentDetails> findByOtherService_IdAndScheduleDateAndServiceStartTime(Integer otherServiceId,
                                                                                   LocalDate scheduleDate, LocalTime serviceStartTime);

    boolean existsByOtherService_IdAndScheduleDateAndServiceStartTimeAndStatus(Integer otherServiceId, LocalDate scheduleDate,
                                                                         LocalTime serviceStartTime, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM AppointmentDetails b WHERE b.garage.id = :garageId AND b.scheduleDate = :date AND b.serviceStartTime = :time AND b.slot.currentBookings < b.slot.maxBookings")
    Optional<OsAppointmentDetails> findLockedSlot(@Param("garageId") Integer otherServiceId, @Param("date") LocalDate date,
                                                @Param("time") LocalTime time);


    @Query("SELECT a.appointmentReferenceId FROM AppointmentDetails a WHERE a.appointmentReferenceId LIKE :prefix% ORDER BY a.appointmentReferenceId DESC")
    List<String> findLatestCodesByPrefix(@Param("prefix")String codePrefix);
}
