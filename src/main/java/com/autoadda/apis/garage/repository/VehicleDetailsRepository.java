/**
 * 
 */
package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.util.Status;

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
