package com.service.garage.apis.controller;

import com.service.garage.apis.entity.GarageSubscription;
import com.service.garage.apis.entity.VehicleOwnerSubscription;
import com.service.garage.apis.request.SubscriptionRequest;
import com.service.garage.apis.service.GarageSubscriptionService;
import com.service.garage.apis.service.VehicleOwnerSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vehicle-subscriptions")
@RequiredArgsConstructor
public class VehicleSubscriptionController {

    private final VehicleOwnerSubscriptionService ownerSubscriptionService;
    private final GarageSubscriptionService garageSubscriptionService;

    @PostMapping("/subscribe")
    ResponseEntity<VehicleOwnerSubscription> subscribeToPlan(@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId
            , @RequestBody SubscriptionRequest request) {
        request.setOwnerId ( Integer.parseInt ( autoAddaId ) );
        return ResponseEntity.ok ( ownerSubscriptionService.subscribeToPlan ( request ) );
    }

    @GetMapping("/owner")
    public List<VehicleOwnerSubscription> getSubscriptionsByOwner(@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ownerSubscriptionService.getSubscriptionsByOwner ( Integer.parseInt ( autoAddaId ) );
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<VehicleOwnerSubscription> getSubscriptionById(@PathVariable Integer subscriptionId) {
        VehicleOwnerSubscription subscription = ownerSubscriptionService.getSubscriptionById ( subscriptionId );
        if ( subscription == null ) {
            return ResponseEntity.notFound ( ).build ( );
        }
        return ResponseEntity.ok ( subscription );
    }

    @PostMapping("/{subscriptionId}/cancel")
    ResponseEntity<Object> cancelSubscription(@PathVariable Integer subscriptionId) {
        ownerSubscriptionService.cancelSubscription ( subscriptionId );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/garage/{garageId}")
    public List<GarageSubscription> getPlansByGarage(@PathVariable Integer garageId) {
        return garageSubscriptionService.getPlansByGarage ( garageId );
    }

    @GetMapping("/all")
    public List<GarageSubscription> getAllPlans() {
        return garageSubscriptionService.getAllPlans ( );
    }

    @GetMapping("/search")
    public List<GarageSubscription> searchPlans(String query) {
        return garageSubscriptionService.searchPlans ( query );
    }

    @GetMapping("/active")
    public List<VehicleOwnerSubscription> getActiveSubscriptions() {
        return ownerSubscriptionService.getActiveSubscriptions ( );
    }

    @GetMapping("/by-date-range/{ownerId}/{startDate}/{endDate}")
    public List<VehicleOwnerSubscription> getSubscriptionsByDateRange(@PathVariable Integer ownerId,
                                                                      @PathVariable LocalDate startDate, @PathVariable LocalDate endDate) {
        return ownerSubscriptionService.getSubscriptionsByDateRange ( ownerId, startDate, endDate );
    }

    @GetMapping("/by-plan/{planId}")
    public List<VehicleOwnerSubscription> getSubscriptionsByPlan(@PathVariable Integer planId) {
        return ownerSubscriptionService.getSubscriptionsByPlan ( planId );
    }

    @GetMapping("/by-owner-and-plan/{ownerId}/{planId}")
    public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndPlan(@PathVariable Integer ownerId,
                                                                         @PathVariable Integer planId) {
        return ownerSubscriptionService.getSubscriptionsByOwnerAndPlan ( ownerId, planId );
    }

    @GetMapping("/by-owner-and-status/{ownerId}/{status}")
    public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndStatus(@PathVariable Integer ownerId,
                                                                           @PathVariable String status) {
        return ownerSubscriptionService.getSubscriptionsByOwnerAndStatus ( ownerId, status );
    }

    @GetMapping("/by-plan-and-status/{planId}/{status}")
    public List<VehicleOwnerSubscription> getSubscriptionsByPlanAndStatus(@PathVariable Integer planId,
                                                                          @PathVariable String status) {
        return ownerSubscriptionService.getSubscriptionsByPlanAndStatus ( planId, status );
    }
}
