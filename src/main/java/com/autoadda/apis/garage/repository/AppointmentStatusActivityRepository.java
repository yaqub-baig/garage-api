package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.AppointmentStatusActivity;

@Repository
public interface AppointmentStatusActivityRepository extends JpaRepository<AppointmentStatusActivity, Integer>{

}
