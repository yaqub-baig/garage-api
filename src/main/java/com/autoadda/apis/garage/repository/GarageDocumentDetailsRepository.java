package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.GarageDocumentDetails;

@Repository
public interface GarageDocumentDetailsRepository extends JpaRepository<GarageDocumentDetails, Integer> {

	List<GarageDocumentDetails> findByGarageId(Integer garageId);
}
