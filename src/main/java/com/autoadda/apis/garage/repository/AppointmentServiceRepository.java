package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentServices, Integer> {

    List<AppointmentServices> findByAppointmentId(Integer appointmentId);

}
