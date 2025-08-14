package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.autoadda.apis.garage.request.SubscriptionRequest;
import com.autoadda.apis.garage.util.SubscriptionStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleOwnerSubscriptionService {

    private final VehicleOwnerRepository vehicleOwnerRepository;

    private final GarageSubscriptionRepository planRepository;

    private final VehicleOwnerSubscriptionRepository vehicleOwnerSubscriptionRepository;

    /**
     * Subscribes a vehicle owner to a garage subscription plan.
     *
     * @param subscription the VehicleOwnerSubscription object containing the
     *                     details of the subscription
     * @return the saved VehicleOwnerSubscription object
     */
    @Transactional
    public VehicleOwnerSubscription subscribeToPlan(SubscriptionRequest subscription) {
        VehicleOwnerSubscription vehicleOwnerSubscription = new VehicleOwnerSubscription ( );

        GarageSubscription plan = planRepository.findById ( subscription.getPlanId ( ) )
                .orElseThrow ( () -> new EntityNotFoundException ( "Subscription plan not found" ) );
        vehicleOwnerSubscription.setGarageSubscription ( plan );
        vehicleOwnerSubscription.setPurchaseDate ( LocalDate.now ( ) );
        vehicleOwnerSubscription.setStartDate ( LocalDate.now ( ) );
        vehicleOwnerSubscription.setEndDate ( LocalDate.now ( ).plusMonths ( plan.getDurationMonths ( ) ) );
        vehicleOwnerSubscription.setStatus ( SubscriptionStatus.ACTIVE );
        vehicleOwnerSubscription.setVehicleOwnerId ( subscription.getOwnerId ( ) );
        return vehicleOwnerSubscriptionRepository.save ( vehicleOwnerSubscription );
    }

    /**
     * Retrieves all subscriptions for a given vehicle owner.
     *
     * @param ownerId the ID of the vehicle owner
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByOwner(Integer ownerId) {
        return vehicleOwnerSubscriptionRepository.findByVehicleOwnerId ( ownerId );
    }

    /**
     * Cancels a subscription by its ID.
     *
     * @param subscriptionId the ID of the subscription to cancel
     */
    @Transactional
    public void cancelSubscription(Integer subscriptionId) {
        VehicleOwnerSubscription subscription = vehicleOwnerSubscriptionRepository.findById ( subscriptionId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Subscription not found" ) );
        subscription.setStatus ( SubscriptionStatus.CANCELLED );
        vehicleOwnerSubscriptionRepository.save ( subscription );
    }

    /**
     * Retrieves a subscription by its ID.
     *
     * @param subscriptionId the ID of the subscription
     * @return the VehicleOwnerSubscription object
     */
    @Transactional
    public VehicleOwnerSubscription getSubscriptionById(Integer subscriptionId) {
        return vehicleOwnerSubscriptionRepository.findById ( subscriptionId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Subscription not found" ) );
    }

    /**
     * Retrieves all active subscriptions.
     *
     * @return a list of active VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getActiveSubscriptions() {
        return vehicleOwnerSubscriptionRepository.findByStatus ( SubscriptionStatus.ACTIVE );
    }

    /**
     * Retrieves all subscriptions for a specific garage subscription plan.
     *
     * @param planId the ID of the garage subscription plan
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByDateRange(Integer ownerId, LocalDate startDate,
                                                                      LocalDate endDate) {
        return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndStartDateBetween ( ownerId, startDate, endDate );
    }

    /**
     * Retrieves all subscriptions for a specific garage subscription plan.
     *
     * @param planId the ID of the garage subscription plan
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByPlan(Integer subscriptionId) {
        return vehicleOwnerSubscriptionRepository.findByGarageSubscriptionId ( subscriptionId );
    }

    /**
     * Retrieves all subscriptions for a specific vehicle owner and garage
     * subscription plan.
     *
     * @param ownerId the ID of the vehicle owner
     * @param planId  the ID of the garage subscription plan
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndPlan(Integer ownerId, Integer subscriptionId) {
        return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndGarageSubscriptionId ( ownerId, subscriptionId );
    }

    /**
     * Retrieves all subscriptions for a specific vehicle owner and status.
     *
     * @param ownerId the ID of the vehicle owner
     * @param status  the status of the subscription
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByOwnerAndStatus(Integer ownerId, String status) {
        return vehicleOwnerSubscriptionRepository.findByVehicleOwnerIdAndStatus ( ownerId, status );
    }

    /**
     * Retrieves all subscriptions for a specific garage subscription plan and
     * status.
     *
     * @param planId the ID of the garage subscription plan
     * @param status the status of the subscription
     * @return a list of VehicleOwnerSubscription objects
     */
    @Transactional
    public List<VehicleOwnerSubscription> getSubscriptionsByPlanAndStatus(Integer subscriptionId, String status) {
        return vehicleOwnerSubscriptionRepository.findByGarageSubscriptionIdAndStatus ( subscriptionId, status );
    }

    /**
     * Updates the status of a subscription.
     *
     * @param subscriptionId the ID of the subscription to update
     * @param status         the new status for the subscription
     * @return the updated VehicleOwnerSubscription object
     */
    @Transactional
    public VehicleOwnerSubscription updateSubscriptionStatus(Integer subscriptionId, SubscriptionStatus status) {
        VehicleOwnerSubscription subscription = vehicleOwnerSubscriptionRepository.findById ( subscriptionId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Subscription not found" ) );

        subscription.setStatus ( status );
        return vehicleOwnerSubscriptionRepository.save ( subscription );
    }

    /**
     * Retrieves a vehicle owner by their ID.
     *
     * @param ownerId the ID of the vehicle owner
     * @return the VehicleOwner object
     */
    @Transactional
    public VehicleOwner getVehicleOwnerById(Integer ownerId) {
        return vehicleOwnerRepository.findById ( ownerId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Vehicle owner not found" ) );
    }

    /**
     * Retrieves all garage subscription plans.
     *
     * @return a list of GarageSubscription objects
     */
    @Transactional
    public List<GarageSubscription> getAllGarageSubscriptions() {
        return planRepository.findAll ( );
    }

    @Transactional
    public List<VehicleOwnerSubscription> getAllSubscriptions() {
        return vehicleOwnerSubscriptionRepository.findAll ( );
    }
}
