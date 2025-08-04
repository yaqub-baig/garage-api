package com.autoadda.apis.garage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GarageSubscriptionService {

	@Autowired
	private final GarageSubscriptionRepository subscriptionRepository;

	@Autowired
	private final GarageRepository garageRepository;

	/**
	 * Retrieves all subscription plans available in the system.
	 *
	 * @return a list of all subscription plans
	 */
	@Transactional
	public List<GarageSubscription> getAllPlans() {
		return subscriptionRepository.findAll();
	}

	/**
	 * Creates a new subscription plan for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @param plan     the subscription plan to be created
	 * @return the created subscription plan
	 */
	@Transactional
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
	@Transactional
	public List<GarageSubscription> getPlansByGarage(Integer garageId) {
//		return subscriptionRepository.findAll().stream().filter(p -> p.getGarage().getId().equals(garageId))
//				.collect(Collectors.toList());

		return subscriptionRepository.findByGarageId(garageId);
	}

	/**
	 * Retrieves a subscription plan by its ID.
	 *
	 * @param id the ID of the subscription plan
	 * @return the subscription plan with the specified ID
	 */
	@Transactional
	public GarageSubscription getPlanById(Integer id) {
		return subscriptionRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));
	}

	/**
	 * Updates an existing subscription plan.
	 *
	 * @param id          the ID of the subscription plan to be updated
	 * @param updatedPlan the updated subscription plan details
	 * @return the updated subscription plan
	 */
	@Transactional
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

	/**
	 * Deletes a subscription plan by its ID.
	 *
	 * @param id the ID of the subscription plan to be deleted
	 */
	@Transactional
	public void deletePlan(Integer id) {
		GarageSubscription existingPlan = getPlanById(id);
		subscriptionRepository.delete(existingPlan);
	}

	/**
	 * Searches for subscription plans by name.
	 *
	 * @param query the search query
	 * @return a list of subscription plans matching the search query
	 */
	@Transactional
	public List<GarageSubscription> searchPlans(String query) {
		return subscriptionRepository.findByNameContainingIgnoreCase(query);
	}

	/**
	 * Filters subscription plans by various criteria.
	 *
	 * @param minPrice       the minimum price of the plans
	 * @param maxPrice       the maximum price of the plans
	 * @param durationMonths the duration in months of the plans
	 * @param description    a keyword to search in the plan descriptions
	 * @param service        a service included in the plans
	 * @return a list of subscription plans matching the filter criteria
	 */
	@Transactional
	public List<GarageSubscription> filterPlansByPriceRange(Double minPrice, Double maxPrice) {
		return subscriptionRepository.findByPriceBetween(minPrice, maxPrice);
	}

	/**
	 * Filters subscription plans by duration in months.
	 *
	 * @param durationMonths the duration in months of the plans
	 * @return a list of subscription plans matching the duration criteria
	 */
	@Transactional
	public List<GarageSubscription> filterPlansByDuration(Integer durationMonths) {
		return subscriptionRepository.findByDurationMonths(durationMonths);
	}

	@Transactional
	public List<GarageSubscription> filterPlansByDescription(String description) {
		return subscriptionRepository.findByDescriptionContainingIgnoreCase(description);
	}

	@Transactional
	public List<GarageSubscription> filterPlansByServicesIncluded(String service) {
		return subscriptionRepository.findByServicesIncludedContainingIgnoreCase(service);
	}

	/**
	 * Retrieves all active subscription plans for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return a list of active subscription plans for the specified garage
	 */
	@Transactional
	public List<GarageSubscription> getActivePlansByGarage(Integer garageId) {
		return subscriptionRepository.findByGarageIdAndIsActive(garageId, true);
	}
}
