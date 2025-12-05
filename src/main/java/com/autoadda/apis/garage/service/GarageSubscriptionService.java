package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageSubscriptionMapper;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.request.GarageSubscriptionRequest;
import com.autoadda.apis.garage.response.GarageSubscriptionResponse;
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


    public static final String SUBSCRIPTION_PLAN_NOT_FOUND = "Subscription plan not found";
    private final GarageSubscriptionRepository subscriptionRepository;

    private final AvailableServiceManagementService availableServiceManagementService;
    private final GarageRepository garageRepository;
    private final GarageSubscriptionMapper garageSubscriptionMapper;

    /**
     * Retrieves all subscription plans available in the system.
     *
     * @return a list of all subscription plans
     */
    @Transactional
    public List<GarageSubscriptionResponse> getAllPlans() {
        return subscriptionRepository.findAll ( )
                .stream()
                .map(garageSubscriptionMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new subscription plan for a specific garage.
     *
     * @param plan the subscription plan to be created
     * @return the created subscription plan
     */
    @Transactional
    public List<GarageSubscriptionResponse> createPlan(GarageSubscriptionRequest plan) {
        List<AvailableService> services = Optional.ofNullable ( plan.getServicesIds ( ) )
                .orElse ( Collections.emptyList ( ) )
                .stream ( )
                .map ( serviceId -> availableServiceManagementService.getServiceById ( serviceId )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "Service not found with ID: " + serviceId ) ) )
                .toList ( );


        // ✅ Use Optional to handle null safely
        return Optional.ofNullable ( plan.getGarageIds ( ) )
                .filter ( ids -> ! ids.isEmpty ( ) )
                .orElseThrow ( () -> new IllegalArgumentException ( "Garage IDs cannot be null or empty" ) )
                .stream ( )
                .map ( garageId -> createSubscriptionForGarage ( plan, garageId, services ) )
                .map(garageSubscriptionMapper::toResponse)
                .toList ( );
    }

    private GarageSubscription createSubscriptionForGarage(GarageSubscriptionRequest plan, Integer garageId, List<AvailableService> services) {
        Garage garage = garageRepository.findById ( garageId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Garage not found with ID: " + garageId ) );

        // Collect all services first

        GarageSubscription newPlan = new GarageSubscription ( );
        newPlan.setName ( plan.getName ( ) );
        newPlan.setDescription ( plan.getDescription ( ) );
        newPlan.setPrice ( plan.getPrice ( ) );
        newPlan.setDurationMonths ( plan.getDurationMonths ( ) );
        newPlan.setGarage ( garage );
        newPlan.setBrands ( plan.getBrands ( ) );
        newPlan.setBenefits ( plan.getBenefits ( ) );
        newPlan.setServicesIncluded ( services ); // Set all at once
        newPlan.setServiceQuantity ( plan.getServices ( ) );
        log.info ( "About to save subscription with " + services.size ( ) + " services" );

        return subscriptionRepository.save ( newPlan );
    }

    /**
     * Retrieves all subscription plans for a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of subscription plans for the specified garage
     */
    @Transactional
    public List<GarageSubscriptionResponse> getPlansByGarage(Integer garageId) {

        return subscriptionRepository.findByGarageId ( garageId )
                .stream()
                .map(garageSubscriptionMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves a subscription plan by its ID.
     *
     * @param id the ID of the subscription plan
     * @return the subscription plan with the specified ID
     */
    @Transactional
    public GarageSubscriptionResponse getPlanById(Integer id) {
        GarageSubscription subscription = subscriptionRepository.findById ( id )
                .orElseThrow ( () -> new EntityNotFoundException (SUBSCRIPTION_PLAN_NOT_FOUND) );
        return garageSubscriptionMapper.toResponse(subscription);
    }

    /**
     * Updates an existing subscription plan.
     *
     * @param id          the ID of the subscription plan to be updated
     * @param updatedPlan the updated subscription plan details
     * @return the updated subscription plan
     */
    @Transactional
    public GarageSubscriptionResponse updatePlan(Integer id, GarageSubscription updatedPlan) {
        GarageSubscription existingPlan = subscriptionRepository.findById ( id )
                .orElseThrow ( () -> new EntityNotFoundException (SUBSCRIPTION_PLAN_NOT_FOUND) );
        existingPlan.setName ( updatedPlan.getName ( ) );
        existingPlan.setDescription ( updatedPlan.getDescription ( ) );
        existingPlan.setPrice ( updatedPlan.getPrice ( ) );
        existingPlan.setDurationMonths ( updatedPlan.getDurationMonths ( ) );
        existingPlan.setServicesIncluded ( updatedPlan.getServicesIncluded ( ) );
        existingPlan.setIsActive ( updatedPlan.getIsActive ( ) );

        GarageSubscription saved = subscriptionRepository.save ( existingPlan );
        return garageSubscriptionMapper.toResponse(saved);
    }

    /**
     * Deletes a subscription plan by its ID.
     *
     * @param id the ID of the subscription plan to be deleted
     */
    @Transactional
    public void deletePlan(Integer id) {
        GarageSubscription existingPlan = subscriptionRepository.findById ( id )
                .orElseThrow ( () -> new EntityNotFoundException (SUBSCRIPTION_PLAN_NOT_FOUND) );
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
    public List<GarageSubscriptionResponse> searchPlans(String brand, String city, String garageName) {
        Set<GarageSubscription> subscriptionPlans = subscriptionRepository.findAllWithLocationAndServices ( );
        List<GarageSubscription> searchResults = new ArrayList<> ( List.of ( ) );
        for (GarageSubscription plan : subscriptionPlans) {
            boolean cityCheck = true;
            boolean nameCheck = true;
            boolean brandCheck = true;

            log.info ( " 2  enabledGarages -> " + subscriptionPlans.size ( ) );
            log.info ( "   currentGarage -> " + plan.getName ( ) );

            Location address = plan.getGarage ( ).getGarageLocation ( );
            log.info ( " 4 enabledGarages -> " );
            if ( StringUtils.isNotEmpty ( city ) ) {
                log.info ( " 5 enabledGarages -> " );
                if ( ! address.getState ( ).toLowerCase ( ).equalsIgnoreCase ( city.toLowerCase ( ) ) ) {
                    cityCheck = false;
                }
            }


            if ( StringUtils.isNotEmpty ( garageName ) && ! plan.getGarage ( ).getName ( ).toLowerCase ( ).contains ( garageName.toLowerCase ( ) ) ) {
                    nameCheck = false;
                }


            if ( StringUtils.isNotEmpty ( brand ) && ! plan.getBrands ( ).toLowerCase ( ).contains ( brand.toLowerCase ( ) ) ) {
                    brandCheck = false;
                }


            if ( cityCheck && nameCheck && brandCheck ) {
                searchResults.add ( plan );
            }
        }
        return searchResults.stream()
                .map(garageSubscriptionMapper::toResponse)
                .toList();
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

    public GarageSubscriptionResponse changeStatus(Integer id) {
        GarageSubscription subscriptionPlan = subscriptionRepository.findById ( id ).orElseThrow ( () -> new EntityNotFoundException (SUBSCRIPTION_PLAN_NOT_FOUND) );
        subscriptionPlan.setIsActive (!Boolean.TRUE.equals(subscriptionPlan.getIsActive()));
        GarageSubscription saved = subscriptionRepository.save ( subscriptionPlan );
        return garageSubscriptionMapper.toResponse(saved);


    }
}
