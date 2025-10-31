package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.request.SubscriptionRequest;
import com.autoadda.apis.garage.response.GarageSubscriptionResponse;
import com.autoadda.apis.garage.response.VehicleOwnerSubscriptionResponse;
import com.autoadda.apis.garage.service.GarageSubscriptionService;
import com.autoadda.apis.garage.service.VehicleOwnerSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin("*")
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
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByOwner(@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
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
    public List<GarageSubscriptionResponse> getPlansByGarage(@PathVariable Integer garageId) {
        return garageSubscriptionService.getPlansByGarage ( garageId );
    }

    @GetMapping("/all")
    public List<GarageSubscriptionResponse> getAllPlans() {
        return garageSubscriptionService.getAllPlans ( );
    }

    @GetMapping("/search")
    public List<GarageSubscriptionResponse> searchPlans(@RequestParam(required = false) String brand, @RequestParam(required = false) String city, @RequestParam(required = false) String garageName) {
        return garageSubscriptionService.searchPlans ( brand, city, garageName );
    }

    @GetMapping("/active")
    public List<VehicleOwnerSubscriptionResponse> getActiveSubscriptions() {
        return ownerSubscriptionService.getActiveSubscriptions ( );
    }

    @GetMapping("/by-date-range/{ownerId}/{startDate}/{endDate}")
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByDateRange(@PathVariable Integer ownerId,
                                                                              @PathVariable LocalDate startDate, @PathVariable LocalDate endDate) {
        return ownerSubscriptionService.getSubscriptionsByDateRange ( ownerId, startDate, endDate );
    }

    @GetMapping("/by-plan/{planId}")
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByPlan(@PathVariable Integer planId) {
        return ownerSubscriptionService.getSubscriptionsByPlan ( planId );
    }

    @GetMapping("/by-owner-and-plan/{ownerId}/{planId}")
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByOwnerAndPlan(@PathVariable Integer ownerId,
                                                                                 @PathVariable Integer planId) {
        return ownerSubscriptionService.getSubscriptionsByOwnerAndPlan ( ownerId, planId );
    }

    @GetMapping("/by-owner-and-status/{ownerId}/{status}")
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByOwnerAndStatus(@PathVariable Integer ownerId,
                                                                                   @PathVariable String status) {
        return ownerSubscriptionService.getSubscriptionsByOwnerAndStatus ( ownerId, status );
    }

    @GetMapping("/by-plan-and-status/{planId}/{status}")
    public List<VehicleOwnerSubscriptionResponse> getSubscriptionsByPlanAndStatus(@PathVariable Integer planId,
                                                                                  @PathVariable String status) {
        return ownerSubscriptionService.getSubscriptionsByPlanAndStatus ( planId, status );
    }
}
