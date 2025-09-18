package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleListing;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleListingRepository extends JpaRepository<VehicleListing, Integer> {
    List<VehicleListing> findByStatus(Status status);

    List<VehicleListing> findByGarageId(Integer garageId);

    List<VehicleListing> findByVin(String vin);

    List<VehicleListing> findByMakeAndModelAndYear(String make, String model, int year);
}