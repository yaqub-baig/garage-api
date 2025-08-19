package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.request.GarageSubscriptionRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarageSubscriptionService {


    private final GarageSubscriptionRepository subscriptionRepository;

    private final AvailableServiceManagementService availableServiceManagementService;
    private final GarageRepository garageRepository;

    /**
     * Retrieves all subscription plans available in the system.
     *
     * @return a list of all subscription plans
     */
    @Transactional
    public List<GarageSubscription> getAllPlans() {
        return subscriptionRepository.findAll ( );
    }

    /**
     * Creates a new subscription plan for a specific garage.
     *
     * @param plan the subscription plan to be created
     * @return the created subscription plan
     */
    @Transactional
    public List<GarageSubscription> createPlan(GarageSubscriptionRequest plan) {

        // ✅ Use Optional to handle null safely
        return Optional.ofNullable ( plan.getGarageIds ( ) )
                .filter ( ids -> ! ids.isEmpty ( ) )
                .orElseThrow ( () -> new IllegalArgumentException ( "Garage IDs cannot be null or empty" ) )
                .stream ( )
                .map ( garageId -> createSubscriptionForGarage ( plan, garageId ) )
                .toList ( );
    }

    private GarageSubscription createSubscriptionForGarage(GarageSubscriptionRequest plan, Integer garageId) {
        Garage garage = garageRepository.findById ( garageId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Garage not found with ID: " + garageId ) );

        // Collect all services first
        List<AvailableService> services = Optional.ofNullable ( plan.getServicesIds ( ) )
                .orElse ( Collections.emptyList ( ) )
                .stream ( )
                .map ( serviceId -> availableServiceManagementService.getServiceById ( serviceId )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "Service not found with ID: " + serviceId ) ) )
                .toList ( );

        GarageSubscription newPlan = new GarageSubscription ( );
        newPlan.setName ( plan.getName ( ) );
        newPlan.setDescription ( plan.getDescription ( ) );
        newPlan.setPrice ( plan.getPrice ( ) );
        newPlan.setDurationMonths ( plan.getDurationMonths ( ) );
        newPlan.setGarage ( garage );
        newPlan.setBrands ( plan.getBrands ( ) );
        newPlan.setBenefits ( plan.getBenefits ( ) );
        newPlan.setServicesIncluded ( services ); // Set all at once

        System.out.println ( "About to save subscription with " + services.size ( ) + " services" );

        return subscriptionRepository.save ( newPlan );
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
        return subscriptionRepository.findByGarageId ( garageId );
    }

    /**
     * Retrieves a subscription plan by its ID.
     *
     * @param id the ID of the subscription plan
     * @return the subscription plan with the specified ID
     */
    @Transactional
    public GarageSubscription getPlanById(Integer id) {
        return subscriptionRepository.findById ( id )
                .orElseThrow ( () -> new EntityNotFoundException ( "Subscription plan not found" ) );
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
        GarageSubscription existingPlan = getPlanById ( id );
        existingPlan.setName ( updatedPlan.getName ( ) );
        existingPlan.setDescription ( updatedPlan.getDescription ( ) );
        existingPlan.setPrice ( updatedPlan.getPrice ( ) );
        existingPlan.setDurationMonths ( updatedPlan.getDurationMonths ( ) );
        existingPlan.setServicesIncluded ( updatedPlan.getServicesIncluded ( ) );
        existingPlan.setIsActive ( updatedPlan.getIsActive ( ) );

        return subscriptionRepository.save ( existingPlan );
    }

    /**
     * Deletes a subscription plan by its ID.
     *
     * @param id the ID of the subscription plan to be deleted
     */
    @Transactional
    public void deletePlan(Integer id) {
        GarageSubscription existingPlan = getPlanById ( id );
        subscriptionRepository.delete ( existingPlan );
    }

    /**
     * Searches for subscription plans by name.
     *
     * @param query               the search query
     * @param city
     * @param zip
     * @param garageName
     * @param availableServiceIds
     * @return a list of subscription plans matching the search query
     */
    @Transactional
    public List<GarageSubscription> searchPlans(String brand, String city, String garageName) {
        Set<GarageSubscription> subscriptionPlans = subscriptionRepository.findAllWithLocationAndServices ( );
        List<GarageSubscription> searchResults = new ArrayList<> ( List.of ( ) );
        for (GarageSubscription plan : subscriptionPlans) {
            boolean cityCheck = true;
            boolean nameCheck = true;
            boolean brandCheck = true;

            log.info ( " 2  enabledGarages -> " + subscriptionPlans.size ( ) );
            log.info ( "   currentGarage -> " + plan.getName ( ) );

            Location address = plan.getGarage ().getGarageLocation ( );
            log.info ( " 4 enabledGarages -> " );
            if ( StringUtils.isNotEmpty ( city ) ) {
                log.info ( " 5 enabledGarages -> " );
                if ( ! address.getCity ( ).toLowerCase ( ).equalsIgnoreCase ( city.toLowerCase ( ) ) ) {
                    cityCheck = false;
                }
            }


            if ( StringUtils.isNotEmpty ( garageName ) ) {
                if ( ! plan.getGarage ( ).getName ().toLowerCase ( ).contains ( garageName.toLowerCase ( ) ) ) {
                    nameCheck = false;
                }
            }

            if ( StringUtils.isNotEmpty ( brand ) ) {
                if ( ! plan.getBrands ( ).toLowerCase ( ).contains ( brand.toLowerCase ( ) ) ) {
                    brandCheck = false;
                }
            }


            if ( cityCheck && nameCheck  && brandCheck ) {
                searchResults.add ( plan );
            }
        }
        return searchResults;
    }

    /**
     * Filters subscription plans by various criteria.
     *
     * @param minPrice the minimum price of the plans
     * @param maxPrice the maximum price of the plans
     * @return a list of subscription plans matching the filter criteria
     */
    @Transactional
    public List<GarageSubscription> filterPlansByPriceRange(Double minPrice, Double maxPrice) {
        return subscriptionRepository.findByPriceBetween ( minPrice, maxPrice );
    }

    /**
     * Filters subscription plans by duration in months.
     *
     * @param durationMonths the duration in months of the plans
     * @return a list of subscription plans matching the duration criteria
     */
    @Transactional
    public List<GarageSubscription> filterPlansByDuration(Integer durationMonths) {
        return subscriptionRepository.findByDurationMonths ( durationMonths );
    }

    @Transactional
    public List<GarageSubscription> filterPlansByDescription(String description) {
        return subscriptionRepository.findByDescriptionContainingIgnoreCase ( description );
    }

    @Transactional
    public List<GarageSubscription> filterPlansByServicesIncluded(Integer garageServiceId) {
        return subscriptionRepository.findByServicesIncludedId ( garageServiceId );
    }

    /**
     * Retrieves all active subscription plans for a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of active subscription plans for the specified garage
     */
    @Transactional
    public List<GarageSubscription> getActivePlansByGarage(Integer garageId) {
        return subscriptionRepository.findByGarageIdAndIsActive ( garageId, true );
    }

    public GarageSubscription changeStatus(Integer id) {
        GarageSubscription subscriptionPlan = subscriptionRepository.findById ( id ).orElseThrow ( () -> new EntityNotFoundException ( "Subscription plan not found" ) );
        if ( subscriptionPlan.getIsActive ( ) )
            subscriptionPlan.setIsActive ( false );
        else
            subscriptionPlan.setIsActive ( true );
        return subscriptionRepository.save ( subscriptionPlan );


    }
}
