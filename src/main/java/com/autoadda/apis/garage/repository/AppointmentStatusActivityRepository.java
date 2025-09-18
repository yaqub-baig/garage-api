package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AppointmentStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentStatusActivityRepository extends JpaRepository<AppointmentStatusActivity, Integer> {

}
