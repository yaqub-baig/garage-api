/**
 * 
 */
package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.VehicleDetails;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Integer> {

	List<VehicleDetails> findByCustomerId(Integer customerId);

	List<VehicleDetails> findByGarageId(Integer garageId);

	List<VehicleDetails> findByVin(String vin);

	List<VehicleDetails> findByYear(String year);

	List<VehicleDetails> findByName(String name);

	List<VehicleDetails> findByOdometer(String odometer);

	List<VehicleDetails> findByColor(String color);

	List<VehicleDetails> findByEngineSize(String engineSize);

	List<VehicleDetails> findByNoOfDoors(String noOfDoors);

	List<VehicleDetails> findByNoOfSeats(String noOfSeats);

	List<VehicleDetails> findByNoOfCylinders(String noOfCylinders);

	List<VehicleDetails> findByNoOfSpeeds(String noOfSpeeds);

	List<VehicleDetails> findByStatus(String status);

	List<VehicleDetails> findByStatusAndGarageId(String status, Integer garageId);
}
