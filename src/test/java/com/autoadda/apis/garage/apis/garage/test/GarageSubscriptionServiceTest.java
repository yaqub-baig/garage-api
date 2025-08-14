package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.request.GarageSubscriptionRequest;
import com.autoadda.apis.garage.service.GarageSubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarageSubscriptionServiceTest {

    @Mock
    private GarageSubscriptionRepository subscriptionRepository;

    @Mock
    private GarageRepository garageRepository;

    @InjectMocks
    private GarageSubscriptionService subscriptionService;

    private Garage garage;
    private GarageSubscription plan;
    private List<AvailableService> services;
    private GarageSubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        subscriptionRequest = new GarageSubscriptionRequest ( );

        garage = new Garage ( );
        garage.setId ( 1 );
        garage.setName ( "Test Garage" );
        services = List.of (
                new AvailableService ( 1, "Oil Change", "skdjfsdf", 20.0, 100.0, null ),
                new AvailableService ( 2, "Oil Change", "skdjfsdf", 20.0, 100.0, null ) );
        plan = new GarageSubscription ( );
        plan.setId ( 1 );
        plan.setName ( "Basic Plan" );
        plan.setGarage ( garage );
        plan.setDescription ( "Simple plan" );
        plan.setPrice ( 100.0 );
        plan.setDurationMonths ( 6 );
        plan.setServicesIncluded ( services );
        plan.setIsActive ( true );
    }

    @Test
    void testGetAllPlans() {
        when ( subscriptionRepository.findAll ( ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> plans = subscriptionService.getAllPlans ( );

        assertEquals ( 1, plans.size ( ) );
        assertEquals ( "Basic Plan", plans.get ( 0 ).getName ( ) );
    }

    @Test
    void testCreatePlan_Success() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( garage ) );
        when ( subscriptionRepository.save ( plan ) ).thenReturn ( plan );

        List<GarageSubscription> created = subscriptionService.createPlan ( subscriptionRequest );

        assertEquals ( plan, created );
        verify ( subscriptionRepository ).save ( plan );
    }

    @Test
    void testCreatePlan_GarageNotFound() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.empty ( ) );

        assertThrows ( EntityNotFoundException.class, () -> subscriptionService.createPlan ( subscriptionRequest ) );
    }

    @Test
    void testGetPlansByGarage() {
        List<GarageSubscription> plans = List.of ( plan );
        when ( subscriptionRepository.findByGarageId ( 1 ) ).thenReturn ( plans );

        List<GarageSubscription> result = subscriptionService.getPlansByGarage ( 1 );

        assertEquals ( 1, result.size ( ) );
        assertEquals ( "Basic Plan", result.get ( 0 ).getName ( ) );
    }

    @Test
    void testGetPlanById_Success() {
        when ( subscriptionRepository.findById ( 1 ) ).thenReturn ( Optional.of ( plan ) );

        GarageSubscription result = subscriptionService.getPlanById ( 1 );

        assertEquals ( "Basic Plan", result.getName ( ) );
    }

    @Test
    void testGetPlanById_NotFound() {
        when ( subscriptionRepository.findById ( 1 ) ).thenReturn ( Optional.empty ( ) );

        assertThrows ( EntityNotFoundException.class, () -> subscriptionService.getPlanById ( 1 ) );
    }

    @Test
    void testUpdatePlan_Success() {
        GarageSubscription updated = new GarageSubscription ( );
        updated.setName ( "Premium" );
        updated.setDescription ( "Extended plan" );
        updated.setPrice ( 150.0 );
        updated.setDurationMonths ( 12 );
        updated.setServicesIncluded ( services );
        updated.setIsActive ( false );

        when ( subscriptionRepository.findById ( 1 ) ).thenReturn ( Optional.of ( plan ) );
        when ( subscriptionRepository.save ( any ( ) ) ).thenAnswer ( inv -> inv.getArgument ( 0 ) );

        GarageSubscription result = subscriptionService.updatePlan ( 1, updated );

        assertEquals ( "Premium", result.getName ( ) );
        assertEquals ( 150.0, result.getPrice ( ) );
        assertEquals ( false, result.getIsActive ( ) );
    }

    @Test
    void testDeletePlan_Success() {
        when ( subscriptionRepository.findById ( 1 ) ).thenReturn ( Optional.of ( plan ) );

        subscriptionService.deletePlan ( 1 );

        verify ( subscriptionRepository ).delete ( plan );
    }

    @Test
    void testSearchPlans() {
        when ( subscriptionRepository.findByNameContainingIgnoreCase ( "basic" ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.searchPlans ( "basic" );

        assertEquals ( 1, result.size ( ) );
        assertEquals ( "Basic Plan", result.get ( 0 ).getName ( ) );
    }

    @Test
    void testFilterByPriceRange() {
        when ( subscriptionRepository.findByPriceBetween ( 50.0, 150.0 ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.filterPlansByPriceRange ( 50.0, 150.0 );

        assertEquals ( 1, result.size ( ) );
    }

    @Test
    void testFilterByDuration() {
        when ( subscriptionRepository.findByDurationMonths ( 6 ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.filterPlansByDuration ( 6 );

        assertEquals ( 1, result.size ( ) );
    }

    @Test
    void testFilterByDescription() {
        when ( subscriptionRepository.findByDescriptionContainingIgnoreCase ( "simple" ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.filterPlansByDescription ( "simple" );

        assertEquals ( 1, result.size ( ) );
    }

    @Test
    void testFilterByServiceIncluded() {
        when ( subscriptionRepository.findByServicesIncludedId ( 10 ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.filterPlansByServicesIncluded ( 10 );

        assertEquals ( 1, result.size ( ) );
    }

    @Test
    void testGetActivePlansByGarage() {
        when ( subscriptionRepository.findByGarageIdAndIsActive ( 1, true ) ).thenReturn ( List.of ( plan ) );

        List<GarageSubscription> result = subscriptionService.getActivePlansByGarage ( 1 );

        assertEquals ( 1, result.size ( ) );
    }
}
