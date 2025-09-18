package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageOwnerRepository extends JpaRepository<GarageOwner, Integer> {

//	List<GarageOwner> findByGarageId(Integer garageId);
}
