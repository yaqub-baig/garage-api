package com.service.garage.apis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.FutureAppointment;
import com.service.garage.apis.util.Status;

public interface FutureAppointmentRepository extends JpaRepository<FutureAppointment, Integer> {
	boolean existsByVehicleIdAndServiceTypeAndStatus(Integer vehicleId, String serviceType, Status status);
}