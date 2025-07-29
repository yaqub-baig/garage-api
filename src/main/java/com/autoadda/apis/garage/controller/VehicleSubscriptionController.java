package com.autoadda.apis.garage.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.request.SubscriptionRequest;
import com.autoadda.apis.garage.service.GarageSubscriptionService;
import com.autoadda.apis.garage.service.VehicleOwnerSubscriptionService;
import com.autoadda.apis.garage.util.SubscriptionStatus;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicle-subscriptions")
@RequiredArgsConstructor
public class VehicleSubscriptionController {

	private final VehicleOwnerSubscriptionService ownerSubscriptionService;
	private final GarageSubscriptionService garageSubscriptionService;

	@PostMapping("/subscribe")
	ResponseEntity<VehicleOwnerSubscription> subscribeToPlan(@RequestBody SubscriptionRequest request) {
		VehicleOwner owner = ownerSubscriptionService.getVehicleOwnerById(request.getOwnerId());
		GarageSubscription plan = garageSubscriptionService.getPlanById(request.getPlanId());

		if (owner == null || plan == null) {
			return ResponseEntity.badRequest().build();
		}
		if (plan.getIsActive() == false) {
			return ResponseEntity.badRequest().body(null);
		}
		VehicleOwnerSubscription subscription = new VehicleOwnerSubscription();
		subscription.setVehicleOwner(owner);
		subscription.setGarageSubscription(plan);
		subscription.setPurchaseDate(LocalDate.now());
		subscription.setStartDate(LocalDate.now());
		subscription.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
		subscription.setStatus(SubscriptionStatus.ACTIVE);

		return ResponseEntity.ok(ownerSubscriptionService.subscribeToPlan(subscription));
	}

	@GetMapping("/owner/{ownerId}")
	public List<VehicleOwnerSubscription> getSubscriptionsByOwner(@PathVariable Integer ownerId) {
		return ownerSubscriptionService.getSubscriptionsByOwner(ownerId);
	}
	
	@GetMapping("/{subscriptionId}")
	public ResponseEntity<VehicleOwnerSubscription> getSubscriptionById(@PathVariable Integer subscriptionId) {
		VehicleOwnerSubscription subscription = ownerSubscriptionService.getSubscriptionById(subscriptionId);
		if (subscription == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(subscription);
	}
	@PostMapping("/{subscriptionId}/cancel")
	ResponseEntity<Object> cancelSubscription(@PathVariable Integer subscriptionId) {
		ownerSubscriptionService.cancelSubscription(subscriptionId);
		return ResponseEntity.noContent().build();
	}
	@GetMapping("/garage/{garageId}")
	public List<GarageSubscription> getPlansByGarage(@PathVariable Integer garageId) {
		return garageSubscriptionService.getPlansByGarage(garageId);
	}
	@GetMapping("/all")
	public List<GarageSubscription> getAllPlans() {
		return garageSubscriptionService.getAllPlans();
	}
	@GetMapping("/search")
	public List<GarageSubscription> searchPlans(String query) {
		return garageSubscriptionService.searchPlans(query);
	}
	@GetMapping("/active")
	public List<VehicleOwnerSubscription> getActiveSubscriptions() {
		return ownerSubscriptionService.getActiveSubscriptions();
	}
	@GetMapping("/by-date-range")
	public List<VehicleOwnerSubscription> getSubscriptionsByDateRange(@PathVariable Integer ownerId,
			@PathVariable LocalDate startDate, @PathVariable LocalDate endDate) {
		return ownerSubscriptionService.getSubscriptionsByDateRange(ownerId, startDate, endDate);
	}
	@GetMapping("/by-plan/{planId}")
		public List<VehicleOwnerSubscription> getSubscriptionsByPlan(@PathVariable Integer planId) {
		return ownerSubscriptionService.getSubscriptionsByPlan(planId);
	}
	@GetMapping("/by-owner-and-plan/{ownerId}/{planId}")
	public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndPlan(@PathVariable Integer ownerId,
			@PathVariable Integer planId) {
		return ownerSubscriptionService.getSubscriptionsByOwnerAndPlan(ownerId, planId);
	}
	@GetMapping("/by-owner-and-status/{ownerId}/{status}")
	public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndStatus(@PathVariable Integer ownerId,
			@PathVariable String status) {
		return ownerSubscriptionService.getSubscriptionsByOwnerAndStatus(ownerId, status);
	}
	@GetMapping("/by-plan-and-status/{planId}/{status}")	
	public List<VehicleOwnerSubscription> getSubscriptionsByPlanAndStatus(@PathVariable Integer planId,
			@PathVariable String status) {
		return ownerSubscriptionService.getSubscriptionsByPlanAndStatus(planId, status);
	}
}
