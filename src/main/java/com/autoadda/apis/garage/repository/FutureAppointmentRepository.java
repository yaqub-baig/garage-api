package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.FutureAppointment;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FutureAppointmentRepository extends JpaRepository<FutureAppointment, Integer> {
    boolean existsByVehicleIdAndServiceTypeAndStatus(Integer vehicleId, String serviceType, Status status);
}