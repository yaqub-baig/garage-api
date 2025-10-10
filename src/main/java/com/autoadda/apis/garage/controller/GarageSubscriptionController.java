package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.request.GarageSubscriptionRequest;
import com.autoadda.apis.garage.response.GarageSubscriptionResponse;
import com.autoadda.apis.garage.service.GarageSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/garage-subscriptions")
@RequiredArgsConstructor
public class GarageSubscriptionController {

    private final GarageSubscriptionService garageSubscriptionService;

    @PostMapping
    public ResponseEntity<List<GarageSubscriptionResponse>> createPlan(@RequestBody GarageSubscriptionRequest plan) {
        if ( plan.getGarageIds ( ) == null || plan.getGarageIds ( ).isEmpty ( ) ) {
            return ResponseEntity.badRequest ( ).body ( null );
        }
        return ResponseEntity.ok ( garageSubscriptionService.createPlan ( plan ) );
    }

    @GetMapping("/garage/{garageId}")
    public List<GarageSubscriptionResponse> getPlansByGarage(@PathVariable Integer garageId) {
        return garageSubscriptionService.getPlansByGarage ( garageId );
    }

//	@PostMapping("/{garageId}")
//	public ResponseEntity<List<GarageSubscription>> createPlan(@PathVariable Integer garageId,
//			@RequestBody GarageSubscription plan) {
//
//		return ResponseEntity.ok(garageSubscriptionService.createPlan(garageId, plan));
//	}

    @GetMapping("/{id}")
    public ResponseEntity<GarageSubscriptionResponse> getPlanById(@PathVariable Integer id) {
        return ResponseEntity.ok ( garageSubscriptionService.getPlanById ( id ) );
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<GarageSubscriptionResponse> updatePlan(@PathVariable Integer id,
                                                         @RequestBody GarageSubscription updatedPlan) {
        return ResponseEntity.ok ( garageSubscriptionService.updatePlan ( id, updatedPlan ) );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<GarageSubscriptionResponse> activatePlan(@PathVariable Integer id) {
        return ResponseEntity.ok ( garageSubscriptionService.changeStatus ( id ) );
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deletePlan(@PathVariable Integer id) {
        garageSubscriptionService.deletePlan ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/search")
    public List<GarageSubscriptionResponse> searchPlans(@RequestParam(required = false) String brand, @RequestParam(required = false) String city, @RequestParam(required = false) String garageName) {
        return garageSubscriptionService.searchPlans ( brand, city, garageName );
    }

    @GetMapping("/all")
    public List<GarageSubscriptionResponse> getAllPlans() {
        return garageSubscriptionService.getAllPlans ( );
    }

}
