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

    List<OsJobCard> findByOsAppointmentId(Integer osAppointmentId);

    List<OsJobCard> findByOtherServiceId(Integer otherServiceId);

    List<OsJobCard> findByOtherServiceIdAndStatus(Integer otherServiceId, Status status);

}
