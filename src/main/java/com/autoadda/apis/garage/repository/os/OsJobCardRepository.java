package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsJobCard;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsJobCardRepository extends JpaRepository<OsJobCard, Integer> {

    List<OsJobCard> findByTechnicianIdAndStatus(Integer technicianId, Status status);

    List<OsJobCard> findByTechnicianId(Integer technicianId);

    List<OsJobCard> findByOsAppointmentId(Integer osAppointmentId);

    List<OsJobCard> findByOtherServiceId(Integer otherServiceId);

    List<OsJobCard> findByOtherServiceIdAndStatus(Integer otherServiceId, Status status);

}
