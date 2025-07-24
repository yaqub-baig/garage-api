package com.autoadda.apis.garage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.service.GarageSubscriptionService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/garage-subscriptions")
@RequiredArgsConstructor
public class GarageSubscriptionController {

	private final GarageSubscriptionService garageSubscriptionService;

	@PostMapping
	public ResponseEntity<GarageSubscription> createPlan(@RequestBody GarageSubscription plan) {
		if (plan.getGarage() == null || plan.getGarage().getId() == null) {
			return ResponseEntity.badRequest().body(null);
		}
		return ResponseEntity.ok(garageSubscriptionService.createPlan(plan.getGarage().getId(), plan));
	}

	@GetMapping("/garage/{garageId}")
	public List<GarageSubscription> getPlansByGarage(@PathVariable Integer garageId) {
		return garageSubscriptionService.getPlansByGarage(garageId);
	}

	@PostMapping("/{garageId}")
	public ResponseEntity<GarageSubscription> createPlan(@PathVariable Integer garageId,
			@RequestBody GarageSubscription plan) {

		return ResponseEntity.ok(garageSubscriptionService.createPlan(garageId, plan));
	}

	@GetMapping("/{id}")
	public ResponseEntity<GarageSubscription> getPlanById(@PathVariable Integer id) {
		return ResponseEntity.ok(garageSubscriptionService.getPlanById(id));
	}

	@PostMapping("/{id}/update")
	public ResponseEntity<GarageSubscription> updatePlan(@PathVariable Integer id,
			@RequestBody GarageSubscription updatedPlan) {
		return ResponseEntity.ok(garageSubscriptionService.updatePlan(id, updatedPlan));
	}

	ResponseEntity<Object> deletePlan(@PathVariable Integer id) {
		garageSubscriptionService.deletePlan(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public List<GarageSubscription> searchPlans(@RequestParam String query) {
		return garageSubscriptionService.searchPlans(query);
	}

	@GetMapping("/all")
	public List<GarageSubscription> getAllPlans() {
		return garageSubscriptionService.getAllPlans();
	}

}
