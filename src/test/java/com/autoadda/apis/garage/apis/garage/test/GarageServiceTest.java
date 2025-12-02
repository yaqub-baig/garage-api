package com.autoadda.apis.garage.apis.garage.test;

//✅ Import JUnit 5 assertions

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

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
        sampleGarageRequest.setWebsite( "http://quickfix.com" );


        sampleGarageEntity = new Garage ( );
        sampleGarageEntity.setId ( 1 );
        sampleGarageEntity.setName ( "QuickFix" );
        sampleGarageEntity.setGarageBrandName ( "QuickFix LLC" );
        sampleGarageEntity.setEmail ( "info@quickfix.com" );
        sampleGarageEntity.setPhoneNumber ( "1234567890" );
        sampleGarageEntity.setWebsite( "http://quickfix.com" );
        sampleGarageEntity.setAccountStatus ( GarageVerificationStatus.PENDING );

    }

    @Test
    void testCreateGarage()  {
        when ( garageRepository.save ( any ( Garage.class ) ) ).thenReturn ( sampleGarageEntity );
        String autoAddaId = "101";
        GarageResponse result = garageService.createGarage ( sampleGarageRequest, autoAddaId ) ;
        assertNotNull ( result );
        assertEquals ( "QuickFix", result.getName ( ) );
        assertEquals ( "QuickFix LLC", result.getGarageBrandName ( ) );
    }

    @Test
    void testGetAllGarages() {
        when ( garageRepository.findAll ( ) ).thenReturn ( List.of ( sampleGarageEntity ) );
        List<GarageResponse> garages = garageService.getAllGarages (  );
        assertEquals ( 1, garages.size ( ) );
        assertEquals ( "QuickFix", garages.get ( 0 ).getName ( ) );
    }

    @Test
    void testGetGarageByIdSuccess() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( sampleGarageEntity ) );
        Optional<GarageResponse> result = garageService.getGarageById ( 1 );
        assertTrue ( result.isPresent ( ) );
        assertEquals ( "QuickFix", result.get ( ).getName ( ) );
    }


    @Test
    void testSoftDeleteGarage() {
        when ( garageRepository.findById ( 1 ) ).thenReturn ( Optional.of ( sampleGarageEntity ) );
        sampleGarageEntity.setAccountStatus ( GarageVerificationStatus.PENDING);
        when ( garageRepository.save ( any ( Garage.class ) ) ).thenReturn ( sampleGarageEntity );

        garageService.deleteGarage ( 1 );
        assertEquals ( GarageVerificationStatus.APPROVED, sampleGarageEntity.getAccountStatus ( ) );
    }
}
