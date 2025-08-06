package com.service.garage.apis.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.GarageSubscription;
import com.service.garage.apis.entity.VehicleOwner;
import com.service.garage.apis.entity.VehicleOwnerSubscription;
import com.service.garage.apis.repository.GarageSubscriptionRepository;
import com.service.garage.apis.repository.VehicleOwnerRepository;
import com.service.garage.apis.repository.VehicleOwnerSubscriptionRepository;
import com.service.garage.apis.util.SubscriptionStatus;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleSubscriptionService {

	private final VehicleOwnerRepository vehicleOwnerRepository;
	private final GarageSubscriptionRepository planRepository;
	private final VehicleOwnerSubscriptionRepository subscriptionRepository;

	public VehicleOwnerSubscription subscribeToPlan(Integer ownerId, Integer planId) {
		VehicleOwner owner = vehicleOwnerRepository.findById(ownerId)
				.orElseThrow(() -> new EntityNotFoundException("Vehicle owner not found"));

		GarageSubscription plan = planRepository.findById(planId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));

		VehicleOwnerSubscription subscription = new VehicleOwnerSubscription();
		subscription.setVehicleOwner(owner);
		subscription.setGarageSubscription(plan);
		subscription.setPurchaseDate(LocalDate.now());
		subscription.setStartDate(LocalDate.now());
		subscription.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
		subscription.setStatus(SubscriptionStatus.ACTIVE);

		return subscriptionRepository.save(subscription);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByOwner(Integer ownerId) {
		return subscriptionRepository.findByVehicleOwnerId(ownerId);
	}

	public void cancelSubscription(Integer subscriptionId) {
		VehicleOwnerSubscription subscription = subscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

		subscription.setStatus(SubscriptionStatus.CANCELLED);
		subscriptionRepository.save(subscription);
	}
}
