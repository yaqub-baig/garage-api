package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleReceived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleReceivedRepository extends JpaRepository<VehicleReceived,Integer> {
    List<VehicleReceived> findByAppointmentGarageId(Integer garageId);
}
