package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.VehicleSale;
import com.service.garage.apis.util.Status;

public interface VehicleSaleRepository extends JpaRepository<VehicleSale, Integer> {

	// Custom query methods can be added here if needed
	// For example, to find by vehicle listing ID or status
	List<VehicleSale> findByVehicleListingId(Integer vehicleListingId);

	List<VehicleSale> findByStatus(Status status);
	// Example: Find by vehicle ID

}