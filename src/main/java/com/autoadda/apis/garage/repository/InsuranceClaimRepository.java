package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.InsuranceClaim;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Integer> {
	List<InsuranceClaim> findByVehicleOwnerId(Integer ownerId);

	List<InsuranceClaim> findByGarageId(Integer garageId);
	List<InsuranceClaim> findByStatus(String status);
	List<InsuranceClaim> findByVehicleOwnerIdAndStatus(Integer ownerId, String status);
	List<InsuranceClaim> findByGarageIdAndStatus(Integer garageId, String status);
	List<InsuranceClaim> findByVehicleOwnerIdAndGarageId(Integer ownerId, Integer garageId);
	
}