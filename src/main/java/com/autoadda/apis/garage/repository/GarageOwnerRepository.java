package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.GarageOwner;

@Repository
public interface GarageOwnerRepository extends JpaRepository<GarageOwner, Integer> {

	List<GarageOwner> findByGarageId(Integer garageId);
}
