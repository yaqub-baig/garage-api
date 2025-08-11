package com.service.garage.apis.controller;

import java.util.List;

import com.service.garage.apis.request.GarageSubscriptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.service.garage.apis.entity.GarageSubscription;
import com.service.garage.apis.service.GarageSubscriptionService;


import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/garage-subscriptions")
@RequiredArgsConstructor
public class GarageSubscriptionController {

	private final GarageSubscriptionService garageSubscriptionService;

	@PostMapping
	public ResponseEntity<List<GarageSubscription>> createPlan(@RequestBody GarageSubscriptionRequest plan) {
		if (plan.getGarageIds() == null || plan.getGarageIds().isEmpty()) {
			return ResponseEntity.badRequest().body ( null );
		}

		return ResponseEntity.ok(garageSubscriptionService.createPlan(plan));
	}

	@GetMapping("/garage/{garageId}")
	public List<GarageSubscription> getPlansByGarage(@PathVariable Integer garageId) {
		return garageSubscriptionService.getPlansByGarage(garageId);
	}

//	@PostMapping("/{garageId}")
//	public ResponseEntity<List<GarageSubscription>> createPlan(@PathVariable Integer garageId,
//			@RequestBody GarageSubscription plan) {
//
//		return ResponseEntity.ok(garageSubscriptionService.createPlan(garageId, plan));
//	}

	@GetMapping("/{id}")
	public ResponseEntity<GarageSubscription> getPlanById(@PathVariable Integer id) {
		return ResponseEntity.ok(garageSubscriptionService.getPlanById(id));
	}

	@PostMapping("/{id}/update")
	public ResponseEntity<GarageSubscription> updatePlan(@PathVariable Integer id,
			@RequestBody GarageSubscription updatedPlan) {
		return ResponseEntity.ok(garageSubscriptionService.updatePlan(id, updatedPlan));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<GarageSubscription> activatePlan(@PathVariable Integer id) {
		return ResponseEntity.ok(garageSubscriptionService.changeStatus(id));
	}

	@DeleteMapping("/{id}")
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
