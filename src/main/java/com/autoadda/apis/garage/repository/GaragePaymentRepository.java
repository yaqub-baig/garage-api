package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GaragePaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GaragePaymentRepository extends JpaRepository<GaragePaymentDetails, Integer> {

    List<GaragePaymentDetails> findByGarageId(Integer garageId);
}
