package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GarageSubscriptionService {

	private final GarageSubscriptionRepository subscriptionRepository;
	private final GarageRepository garageRepository;

	public List<GarageSubscription> getAllPlans() {
		return subscriptionRepository.findAll();
	}

	public GarageSubscription createPlan(Integer garageId, GarageSubscription plan) {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new EntityNotFoundException("Garage not found"));

		plan.setGarage(garage);
		return subscriptionRepository.save(plan);
	}

	/**
	 * Retrieves all subscription plans for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return a list of subscription plans for the specified garage
	 */
	public List<GarageSubscription> getPlansByGarage(Integer garageId) {
		return subscriptionRepository.findAll().stream().filter(p -> p.getGarage().getId().equals(garageId))
				.collect(Collectors.toList());
	}

	public GarageSubscription getPlanById(Integer id) {
		return subscriptionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));
	}

	public GarageSubscription updatePlan(Integer id, GarageSubscription updatedPlan) {
		GarageSubscription existingPlan = getPlanById(id);
		existingPlan.setName(updatedPlan.getName());
		existingPlan.setDescription(updatedPlan.getDescription());
		existingPlan.setPrice(updatedPlan.getPrice());
		existingPlan.setDurationMonths(updatedPlan.getDurationMonths());
		existingPlan.setServicesIncluded(updatedPlan.getServicesIncluded());
		existingPlan.setIsActive(updatedPlan.getIsActive());

		return subscriptionRepository.save(existingPlan);
	}

	public void deletePlan(Integer id) {
		GarageSubscription existingPlan = getPlanById(id);
		subscriptionRepository.delete(existingPlan);
	}

	public List<GarageSubscription> searchPlans(String query) {
		return subscriptionRepository.findByNameContainingIgnoreCase(query);
	}

	public List<GarageSubscription> filterPlansByPriceRange(Double minPrice, Double maxPrice) {
		return subscriptionRepository.findByPriceBetween(minPrice, maxPrice);
	}

	public List<GarageSubscription> filterPlansByDuration(Integer durationMonths) {
		return subscriptionRepository.findByDurationMonths(durationMonths);
	}

	public List<GarageSubscription> filterPlansByDescription(String description) {
		return subscriptionRepository.findByDescriptionContainingIgnoreCase(description);
	}

	public List<GarageSubscription> filterPlansByServicesIncluded(String service) {
		return subscriptionRepository.findByServicesIncludedContainingIgnoreCase(service);
	}

	public List<GarageSubscription> getActivePlansByGarage(Integer garageId) {
		return subscriptionRepository.findByGarageIdAndIsActive(garageId, true);
	}
}
