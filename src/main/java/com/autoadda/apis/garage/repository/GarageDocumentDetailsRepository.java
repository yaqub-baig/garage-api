package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageDocumentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageDocumentDetailsRepository extends JpaRepository<GarageDocumentDetails, Integer> {

    List<GarageDocumentDetails> findByGarageId(Integer garageId);
}
