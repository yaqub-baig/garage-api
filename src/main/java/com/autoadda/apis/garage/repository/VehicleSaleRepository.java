package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleSale;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleSaleRepository extends JpaRepository<VehicleSale, Integer> {

    // Custom query methods can be added here if needed
    // For example, to find by vehicle listing ID or status
    List<VehicleSale> findByVehicleListingId(Integer vehicleListingId);

    List<VehicleSale> findByStatus(Status status);
    // Example: Find by vehicle ID

}