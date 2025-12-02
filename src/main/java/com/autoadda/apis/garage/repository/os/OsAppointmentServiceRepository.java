package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsAppointmentServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OsAppointmentServiceRepository extends JpaRepository<OsAppointmentServices, Integer>{

    List<OsAppointmentServices> findByAppointmentId(Integer id);
}
