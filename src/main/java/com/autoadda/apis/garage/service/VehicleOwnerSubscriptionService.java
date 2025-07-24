package com.autoadda.apis.garage.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.autoadda.apis.garage.util.SubscriptionStatus;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleOwnerSubscriptionService {

	private final VehicleOwnerRepository vehicleOwnerRepository;
	private final GarageSubscriptionRepository planRepository;
	private final VehicleOwnerSubscriptionRepository vehicleOwnerSubscriptionRepository;

	public VehicleOwnerSubscription subscribeToPlan(VehicleOwnerSubscription subscription) {
		VehicleOwner owner = vehicleOwnerRepository.findById(subscription.getVehicleOwner().getId())
				.orElseThrow(() -> new EntityNotFoundException("Vehicle owner not found"));

		GarageSubscription plan = planRepository.findById(subscription.getGarageSubscription().getId())
				.orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));

		subscription.setVehicleOwner(owner);
		subscription.setGarageSubscription(plan);
		subscription.setPurchaseDate(LocalDate.now());
		subscription.setStartDate(LocalDate.now());
		subscription.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
		subscription.setStatus(SubscriptionStatus.ACTIVE);

		return vehicleOwnerSubscriptionRepository.save(subscription);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByOwner(Integer ownerId) {
		return vehicleOwnerSubscriptionRepository.findByVehicleOwnerId(ownerId);
	}

	public void cancelSubscription(Integer subscriptionId) {
		VehicleOwnerSubscription subscription = vehicleOwnerSubscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

		subscription.setStatus(SubscriptionStatus.CANCELLED);
		vehicleOwnerSubscriptionRepository.save(subscription);
	}

	public VehicleOwnerSubscription getSubscriptionById(Integer subscriptionId) {
		return vehicleOwnerSubscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found"));
	}

	public List<VehicleOwnerSubscription> getActiveSubscriptions() {
		return vehicleOwnerSubscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByDateRange(Integer ownerId, LocalDate startDate,
			LocalDate endDate) {
		return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndStartDateBetween(ownerId, startDate, endDate);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByPlan(Integer planId) {
		return vehicleOwnerSubscriptionRepository.findByGarageSubscriptionId(planId);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndPlan(Integer ownerId, Integer planId) {
		return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndGarageSubscriptionId(ownerId, planId);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndStatus(Integer ownerId, String status) {
		return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndStatus(ownerId, status);
	}

	public List<VehicleOwnerSubscription> getSubscriptionsByPlanAndStatus(Integer planId, String status) {
		return vehicleOwnerSubscriptionRepository.findByGarageSubscriptionIdAndStatus(planId, status);
	}

	public VehicleOwnerSubscription updateSubscriptionStatus(Integer subscriptionId, SubscriptionStatus status) {
		VehicleOwnerSubscription subscription = vehicleOwnerSubscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

		subscription.setStatus(status);
		return vehicleOwnerSubscriptionRepository.save(subscription);
	}

	public VehicleOwner getVehicleOwnerById(Integer ownerId) {
		return vehicleOwnerRepository.findById(ownerId)
				.orElseThrow(() -> new EntityNotFoundException("Vehicle owner not found"));
	}

}
