package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.GaragePaymentDetails;

@Repository
public interface GaragePaymentRepository extends JpaRepository<GaragePaymentDetails, Integer> {

	List<GaragePaymentDetails> findByGarageId(Integer garageId);
}
