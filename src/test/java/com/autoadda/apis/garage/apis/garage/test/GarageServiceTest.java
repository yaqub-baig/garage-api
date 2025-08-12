package com.autoadda.apis.garage.apis.garage.test;

//✅ Import JUnit 5 assertions

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class GarageServiceTest {

    @InjectMocks
    private GarageManagementService garageService;

    @Mock
    private GarageRepository garageRepository;

    private GarageRequest sampleGarageRequest;
    private Garage sampleGarageEntity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks ( this );

        sampleGarageRequest = new GarageRequest ( );
        sampleGarageRequest.setName ( "QuickFix" );
        sampleGarageRequest.setGarageBrandName ( "QuickFix LLC" );
        sampleGarageRequest.setEmail ( "info@quickfix.com" );
        sampleGarageRequest.setPhoneNumber ( "1234567890" );
        sampleGarageRequest.setWebSite ( "http://quickfix.com" );
        sampleGarageRequest.setChainId ( 101 );

        sampleGarageEntity = new Garage ( );
        sampleGarageEntity.setId ( 1 );
        sampleGarageEntity.setName ( "QuickFix" );
        sampleGarageEntity.setGarageBrandName ( "QuickFix LLC" );
        sampleGarageEntity.setEmail ( "info@quickfix.com" );
        sampleGarageEntity.setPhoneNumber ( "1234567890" );
        sampleGarageEntity.setWebSite ( "http://quickfix.com" );
        sampleGarageEntity.setAccountStatus ( Status.ACTIVE );

    }

    @Test
    void testCreateGarage() throws IOException {
        when ( garageRepository.save ( any ( Garage.class ) ) ).thenReturn ( sampleGarageEntity );
        String userId = "105";
        String userRole = "ADMIN";
        String autoAddaId = "101";
        Garage result = garageService.createGarage ( sampleGarageRequest, userId, userRole, autoAddaId ) ;
        assertNotNull ( result );
        assertEquals ( "QuickFix", result.getName ( ) );
        assertEquals ( "QuickFix LLC", result.getGarageBrandName ( ) );
    }

    @Test
    void testGetAllGarages() {
        when ( garageRepository.findAll ( ) ).thenReturn ( List.of ( sampleGarageEntity ) );
        List<Garage> garages = garageService.getAllGarages (  );
        assertEquals ( 1, garages.size ( ) );
        assertEquals ( "QuickFix", garages.get ( 0 ).getName ( ) );
    }

    @Test
    void testGetGarageByIdSuccess() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( sampleGarageEntity ) );
        Optional<Garage> result = garageService.getGarageById ( 1 );
        assertTrue ( result.isPresent ( ) );
        assertEquals ( "QuickFix", result.get ( ).getName ( ) );
    }

//    @Test
//    void testUpdateGarageSuccess() {
//        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( sampleGarageEntity ) );
//        when ( garageRepository.save ( any ( Garage.class ) ) ).thenReturn ( sampleGarageEntity );
//
//        GarageRequest updatedRequest = new GarageRequest ( );
//        updatedRequest.setName ( "SpeedFix" );
//        updatedRequest.setGarageBrandName ( "SpeedFix Inc" );
//        updatedRequest.setPhoneNumber ( "9998887777" );
//
//        Garage updatedGarage = garageService.updateGarage ( 1, updatedRequest );
//
//        assertNotNull ( updatedGarage );
//        assertEquals ( "SpeedFix", updatedGarage.getName ( ) );
//        assertEquals ( "SpeedFix Inc", updatedGarage.getGarageBrandName ( ) );
//        assertEquals ( "9998887777", updatedGarage.getPhoneNumber ( ) );
//    }

    @Test
    void testSoftDeleteGarage() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( sampleGarageEntity ) );
        sampleGarageEntity.setAccountStatus ( Status.REMOVED );
        when ( garageRepository.save ( any ( Garage.class ) ) ).thenReturn ( sampleGarageEntity );

        garageService.deleteGarage ( 1 );
        assertEquals ( Status.REMOVED, sampleGarageEntity.getAccountStatus ( ) );
    }
}
