/**
 * 
 */
package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.VehicleDetails;
import com.service.garage.apis.util.Status;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Integer> {

	List<VehicleDetails> findByVin(String vin);

	List<VehicleDetails> findByYear(Integer year);

	List<VehicleDetails> findByName(String name);

	List<VehicleDetails> findByOdometer(Integer odometer);

	List<VehicleDetails> findByColor(String color);

	List<VehicleDetails> findByEngineSize(String engineSize);

	List<VehicleDetails> findByNoOfDoors(Integer noOfDoors);

	List<VehicleDetails> findByNoOfSeats(Integer noOfSeats);

	List<VehicleDetails> findByNoOfCylinders(Integer noOfCylinders);

	List<VehicleDetails> findByNoOfSpeeds(Integer noOfSpeeds);

	List<VehicleDetails> findByStatus(Status status);

}
