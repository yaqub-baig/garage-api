package com.autoadda.apis.garage.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.util.SubscriptionStatus;

@Repository
public interface VehicleOwnerSubscriptionRepository extends JpaRepository<VehicleOwnerSubscription, Integer> {
	List<VehicleOwnerSubscription> findByCustomerId(Integer ownerId);

	List<VehicleOwnerSubscription> findByGarageSubscriptionId(Integer subscriptionId);

	List<VehicleOwnerSubscription> findByCustomerIdAndGarageSubscriptionId(Integer ownerId, Integer subscriptionId);

	List<VehicleOwnerSubscription> findByCustomerIdAndStatus(Integer ownerId, String status);

	List<VehicleOwnerSubscription> findByGarageSubscriptionIdAndStatus(Integer subscriptionId, String status);

	List<VehicleOwnerSubscription> findByCustomerIdAndStatusAndGarageSubscriptionId(Integer ownerId, String status,
			Integer subscriptionId);

	List<VehicleOwnerSubscription> findByStatus(SubscriptionStatus status);

	List<VehicleOwnerSubscription> findByCustomerIdAndStartDateBetween(Integer ownerId, LocalDate startDate,
			LocalDate endDate);

}
