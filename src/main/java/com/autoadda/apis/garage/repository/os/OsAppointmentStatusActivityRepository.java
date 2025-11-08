package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsAppointmentStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OsAppointmentStatusActivityRepository extends JpaRepository<OsAppointmentStatusActivity, Integer>{

}
