package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.VehicleListing;
import com.service.garage.apis.util.Status;

public interface VehicleListingRepository extends JpaRepository<VehicleListing, Integer> {
	List<VehicleListing> findByStatus(Status status);

	List<VehicleListing> findByGarageId(Integer garageId);

	List<VehicleListing> findByVin(String vin);

	List<VehicleListing> findByMakeAndModelAndYear(String make, String model, int year);
}