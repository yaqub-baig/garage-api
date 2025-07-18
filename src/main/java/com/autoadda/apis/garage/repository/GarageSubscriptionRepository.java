package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.GarageSubscription;

@Repository
public interface GarageSubscriptionRepository extends JpaRepository<GarageSubscription, Integer> {
	
	GarageSubscription findByGarageId(Integer garageId);
}
