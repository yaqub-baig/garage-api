package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.FutureAppointment;
import com.autoadda.apis.garage.util.Status;

public interface FutureAppointmentRepository extends JpaRepository<FutureAppointment, Integer> {
	boolean existsByVehicleIdAndServiceTypeAndStatus(Integer vehicleId, String serviceType, Status status);
}