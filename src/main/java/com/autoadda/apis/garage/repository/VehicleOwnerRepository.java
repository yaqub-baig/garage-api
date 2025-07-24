package com.autoadda.apis.garage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.util.OwnerType;

@Repository
public interface VehicleOwnerRepository extends JpaRepository<VehicleOwner, Integer> {

	Optional<VehicleOwner> findByEmail(String email);

	List<VehicleOwner> findByOwnerType(OwnerType ownerType);

	List<VehicleOwner> findByEmailContainingIgnoreCase(String email);

	List<VehicleOwner> findByEmailContainingIgnoreCaseAndOwnerType(String email, OwnerType ownerType);

	List<VehicleOwner> findByEmailContainingIgnoreCaseAndOwnerTypeAndIdNot(String email, OwnerType ownerType,
			Integer id);

	List<VehicleOwner> findByMobileNumberContainingIgnoreCase(String mobileNumber);
}
