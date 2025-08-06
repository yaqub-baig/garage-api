package com.service.garage.apis.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.VehicleOwnerSubscription;
import com.service.garage.apis.util.SubscriptionStatus;

@Repository
public interface VehicleOwnerSubscriptionRepository extends JpaRepository<VehicleOwnerSubscription, Integer> {
	List<VehicleOwnerSubscription> findByVehicleOwnerId(Integer ownerId);

	List<VehicleOwnerSubscription> findByGarageSubscriptionId(Integer subscriptionId);

	List<VehicleOwnerSubscription> findByVehicleOwnerIdAndGarageSubscriptionId(Integer ownerId, Integer subscriptionId);

	List<VehicleOwnerSubscription> findByVehicleOwnerIdAndStatus(Integer ownerId, String status);

	List<VehicleOwnerSubscription> findByGarageSubscriptionIdAndStatus(Integer subscriptionId, String status);

	List<VehicleOwnerSubscription> findByVehicleOwnerIdAndStatusAndGarageSubscriptionId(Integer ownerId, String status,
			Integer subscriptionId);

	List<VehicleOwnerSubscription> findByStatus(SubscriptionStatus status);

	List<VehicleOwnerSubscription> findByVehicleOwnerIdAndStartDateBetween(Integer ownerId, LocalDate startDate,
			LocalDate endDate);

}
