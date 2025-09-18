package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.RoadsideAssistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadsideAssistanceRepository extends JpaRepository<RoadsideAssistance, Integer> {
    List<RoadsideAssistance> findByVehicleOwnerId(Integer vehicleOwnerId);
}
