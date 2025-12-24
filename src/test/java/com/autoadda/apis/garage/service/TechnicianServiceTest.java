package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.TechnicianMapper;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.TechnicianResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.ERole;
import com.autoadda.apis.garage.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianMapper technicianMapper;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private GarageRepository garageRepo;

    @Mock
    private AvailableServiceRepository availableServiceRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private TechnicianService technicianService;

    private TechnicianRequest technicianRequest;
    private Technician technician;
    private TechnicianResponse technicianResponse;
    private Garage garage;
    private UserResponse userResponse;
    private ResponseEntity<UserResponse> userResponseEntity;

    @BeforeEach
    void setUp() {
        technicianRequest = TechnicianRequest.builder ( )
                .emailId ( "tech@example.com" )
                .firstName ( "John" )
                .mobileNumber ( "+1234567890" )
                .emiratesId ( "784-1234-5678901-1" )
                .status ( Status.ACTIVE )
                .employeeRole ( EMPLOYEE_ROLE.SUPERVISOR )
                .garageId ( 1 )
                .skillSetIds ( Arrays.asList ( 1, 2, 3 ) )
                .build ( );

        garage = Garage.builder ( )
                .id ( 1 )
                .name ( "Test Garage" )
                .build ( );

        technician = Technician.builder ( )
                .id ( 1 )
                .emailId ( "tech@example.com" )
                .firstName ( "John" )
                .mobileNumber ( "+1234567890" )
                .emiratesId ( "784-1234-5678901-1" )
                .status ( Status.ACTIVE )
                .employeeRole ( EMPLOYEE_ROLE.SUPERVISOR )
                .garages ( Set.of(garage) )
                .autoAddaId ( 1001 )
                .build ( );

        technicianResponse = TechnicianResponse.builder ( )
                .id ( 1 )
                .emailId ( "tech@example.com" )
                .firstName ( "John" )
                .lastName ( "Doe" )
                .mobileNumber ( "+1234567890" )
                .status ( Status.ACTIVE )
                .employeeRole ( EMPLOYEE_ROLE.SUPERVISOR )
                .build ( );

        userResponse = UserResponse.builder ( )
                .id ( 1001 )
                .mobileNumber ( "+1234567890" )
                .build ( );

        userResponseEntity = ResponseEntity.ok ( userResponse );
    }

    @Test
    void getTechniciansByGarage_ShouldReturnListOfTechnicians() {
        // Arrange
        List<Technician> technicians = Arrays.asList ( technician );
        when ( technicianRepository.findByGaragesContaining ( 1 ) ).thenReturn ( technicians );
        when ( technicianMapper.entityToResponse ( any ( Technician.class ) ) ).thenReturn ( technicianResponse );

        // Act
        List<TechnicianResponse> result = technicianService.getTechniciansByGarage ( 1 );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        assertEquals ( "tech@example.com", result.get ( 0 ).getEmailId ( ) );
        verify ( technicianRepository ).findByGaragesContaining ( 1 );
        verify ( technicianMapper, times ( 1 ) ).entityToResponse ( any ( Technician.class ) );
    }

    @Test
    void getTechniciansByGarage_WhenNoTechnicians_ShouldReturnEmptyList() {
        // Arrange
        when ( technicianRepository.findByGaragesContaining ( 1 ) ).thenReturn ( Collections.emptyList ( ) );

        // Act
        List<TechnicianResponse> result = technicianService.getTechniciansByGarage ( 1 );

        // Assert
        assertNotNull ( result );
        assertTrue ( result.isEmpty ( ) );
        verify ( technicianRepository ).findByGaragesContaining ( 1 );
        verify ( technicianMapper, never ( ) ).entityToResponse ( any ( ) );
    }

    @Test
    void onboardTechnician_ShouldSuccessfullyOnboardTechnician() {
        // Arrange
        when ( garageRepo.findById ( 1 ) ).thenReturn ( Optional.of ( garage ) );
        when ( availableServiceRepository.findAllById ( anyList ( ) ) ).thenReturn ( Collections.emptyList ( ) );
        when ( authClient.getUserDetails ( "+1234567890" ) ).thenReturn ( userResponseEntity );
        when ( technicianRepository.saveAndFlush ( any ( Technician.class ) ) ).thenReturn ( technician );
        when ( technicianMapper.entityToResponse ( any ( Technician.class ) ) ).thenReturn ( technicianResponse );

        // Act
        TechnicianResponse result = technicianService.onboardTechnician ( technicianRequest );

        // Assert
        assertNotNull ( result );
        assertEquals ( "tech@example.com", result.getEmailId ( ) );
        verify ( garageRepo ).findById ( 1 );
        verify ( authClient ).getUserDetails ( "+1234567890" );
        verify ( authClient ).updateRole ( 1001, ERole.GARAGE_TECH );
        verify ( technicianRepository ).saveAndFlush ( any ( Technician.class ) );
    }

    @Test
    void onboardTechnician_WhenGarageNotFound_ShouldThrowException() {
        // Arrange
        when ( garageRepo.findById ( 1 ) ).thenReturn ( Optional.empty ( ) );

        // Act & Assert
        assertThrows ( ResourceNotFoundException.class, () -> {
            technicianService.onboardTechnician ( technicianRequest );
        } );
        verify ( garageRepo ).findById ( 1 );
        verify ( technicianRepository, never ( ) ).saveAndFlush ( any ( ) );
    }

    @Test
    void onboardTechnician_WhenUserNotFoundInAuthService_ShouldThrowException() {
        // Arrange
        when ( garageRepo.findById ( 1 ) ).thenReturn ( Optional.of ( garage ) );
        when ( availableServiceRepository.findAllById ( anyList ( ) ) ).thenReturn ( Collections.emptyList ( ) );
        when ( authClient.getUserDetails ( "+1234567890" ) ).thenReturn ( ResponseEntity.status ( HttpStatus.NOT_FOUND ).build ( ) );

        // Act & Assert
        assertThrows ( ResourceNotFoundException.class, () -> {
            technicianService.onboardTechnician ( technicianRequest );
        } );
        verify ( garageRepo ).findById ( 1 );
        verify ( authClient ).getUserDetails ( "+1234567890" );
        verify ( authClient, never ( ) ).updateRole ( anyInt ( ), any ( ) );
        verify ( technicianRepository, never ( ) ).saveAndFlush ( any ( ) );
    }

    @Test
    void findByEmail_WhenTechnicianExists_ShouldReturnTechnician() {
        // Arrange
        when ( technicianRepository.findByEmailId ( "tech@example.com" ) ).thenReturn ( Optional.of ( technician ) );
        when ( technicianMapper.entityToResponse ( technician ) ).thenReturn ( technicianResponse );

        // Act
        Optional<TechnicianResponse> result = technicianService.findByEmail ( "tech@example.com" );

        // Assert
        assertTrue ( result.isPresent ( ) );
        assertEquals ( "tech@example.com", result.get ( ).getEmailId ( ) );
        verify ( technicianRepository ).findByEmailId ( "tech@example.com" );
    }

    @Test
    void findByEmail_WhenTechnicianNotExists_ShouldReturnEmpty() {
        // Arrange
        when ( technicianRepository.findByEmailId ( "nonexistent@example.com" ) ).thenReturn ( Optional.empty ( ) );

        // Act
        Optional<TechnicianResponse> result = technicianService.findByEmail ( "nonexistent@example.com" );

        // Assert
        assertFalse ( result.isPresent ( ) );
        verify ( technicianRepository ).findByEmailId ( "nonexistent@example.com" );
    }

    @Test
    void deleteTechnician_ShouldCallRepositoryDelete() {
        // Act
        technicianService.deleteTechnician ( 1 );

        // Assert
        verify ( technicianRepository ).deleteById ( 1 );
    }

    @Test
    void findById_WhenTechnicianExists_ShouldReturnTechnician() {
        // Arrange
        when ( technicianRepository.findById ( 1 ) ).thenReturn ( Optional.of ( technician ) );
        when ( technicianMapper.entityToResponse ( technician ) ).thenReturn ( technicianResponse );

        // Act
        Optional<TechnicianResponse> result = technicianService.findById ( 1 );

        // Assert
        assertTrue ( result.isPresent ( ) );
        assertEquals ( 1, result.get ( ).getId ( ) );
        verify ( technicianRepository ).findById ( 1 );
    }

    @Test
    void findById_WhenTechnicianNotExists_ShouldReturnEmpty() {
        // Arrange
        when ( technicianRepository.findById ( 999 ) ).thenReturn ( Optional.empty ( ) );

        // Act
        Optional<TechnicianResponse> result = technicianService.findById ( 999 );

        // Assert
        assertFalse ( result.isPresent ( ) );
        verify ( technicianRepository ).findById ( 999 );
    }

    @Test
    void findBySkillSet_ShouldReturnTechniciansWithSkill() {
        // Arrange
        List<Technician> technicians = Arrays.asList ( technician );
        when ( technicianRepository.findBySkillSetId ( 1 ) ).thenReturn ( technicians );
        when ( technicianMapper.entityToResponse ( any ( Technician.class ) ) ).thenReturn ( technicianResponse );

        // Act
        List<TechnicianResponse> result = technicianService.findBySkillSet ( 1 );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        verify ( technicianRepository ).findBySkillSetId ( 1 );
    }

    @Test
    void findByStatus_ShouldReturnTechniciansWithStatus() {
        // Arrange
        List<Technician> technicians = Arrays.asList ( technician );
        when ( technicianRepository.findByStatus ( "ACTIVE" ) ).thenReturn ( technicians );
        when ( technicianMapper.entityToResponse ( any ( Technician.class ) ) ).thenReturn ( technicianResponse );

        // Act
        List<TechnicianResponse> result = technicianService.findByStatus ( "ACTIVE" );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        verify ( technicianRepository ).findByStatus ( "ACTIVE" );
    }

    @Test
    void findByPhone_WhenTechnicianExists_ShouldReturnTechnician() {
        // Arrange
        when ( technicianRepository.findByMobileNumber ( "+1234567890" ) ).thenReturn ( Optional.of ( technician ) );

        // Act
        Optional<Technician> result = technicianService.findByPhone ( "+1234567890" );

        // Assert
        assertTrue ( result.isPresent ( ) );
        assertEquals ( "+1234567890", result.get ( ).getMobileNumber ( ) );
        verify ( technicianRepository ).findByMobileNumber ( "+1234567890" );
    }

    @Test
    void updateTechnician_ShouldUpdateExistingTechnician() {
        // Arrange
        TechnicianRequest updateRequest = TechnicianRequest.builder ( )
                .firstName ( "Updated" )

                .emailId ( "updated@example.com" )
                .skillSetIds ( Arrays.asList ( 4, 5 ) )
                .build ( );

        when ( technicianRepository.findById ( 1 ) ).thenReturn ( Optional.of ( technician ) );
        when ( technicianMapper.updateTechnician ( any ( Technician.class ), any ( TechnicianRequest.class ) ) )
                .thenReturn ( technician );
        when ( availableServiceRepository.findAllById ( anyList ( ) ) ).thenReturn ( Collections.emptyList ( ) );
        when ( technicianRepository.save ( any ( Technician.class ) ) ).thenReturn ( technician );
        when ( technicianMapper.entityToResponse ( any ( Technician.class ) ) ).thenReturn ( technicianResponse );

        // Act
        TechnicianResponse result = technicianService.updateTechnician ( 1, updateRequest );

        // Assert
        assertNotNull ( result );
        verify ( technicianRepository ).findById ( 1 );
        verify ( technicianMapper ).updateTechnician ( any ( Technician.class ), any ( TechnicianRequest.class ) );
        verify ( availableServiceRepository ).findAllById ( Arrays.asList ( 4, 5 ) );
        verify ( technicianRepository ).save ( any ( Technician.class ) );
    }

    @Test
    void updateTechnician_WhenTechnicianNotFound_ShouldThrowException() {
        // Arrange
        when ( technicianRepository.findById ( 999 ) ).thenReturn ( Optional.empty ( ) );

        // Act & Assert
        assertThrows ( ResourceNotFoundException.class, () -> {
            technicianService.updateTechnician ( 999, technicianRequest );
        } );
        verify ( technicianRepository ).findById ( 999 );
    }

    @Test
    void getAllTechnicians_ShouldReturnAllTechnicians() {
        // Arrange
        List<Technician> technicians = Arrays.asList ( technician );
        when ( technicianRepository.findAll ( ) ).thenReturn ( technicians );

        // Act
        List<Technician> result = technicianService.getAllTechnicians ( );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.size ( ) );
        verify ( technicianRepository ).findAll ( );
    }

    @Test
    void getTechnicianById_WhenTechnicianExists_ShouldReturnTechnician() {
        // Arrange
        when ( technicianRepository.findById ( 1 ) ).thenReturn ( Optional.of ( technician ) );

        // Act
        Technician result = technicianService.getTechnicianById ( 1 );

        // Assert
        assertNotNull ( result );
        assertEquals ( 1, result.getId ( ) );
        verify ( technicianRepository ).findById ( 1 );
    }

    @Test
    void getTechnicianById_WhenTechnicianNotExists_ShouldThrowException() {
        // Arrange
        when ( technicianRepository.findById ( 999 ) ).thenReturn ( Optional.empty ( ) );

        // Act & Assert
        assertThrows ( RuntimeException.class, () -> {
            technicianService.getTechnicianById ( 999 );
        } );
        verify ( technicianRepository ).findById ( 999 );
    }

    @Test
    void mapToEntity_ShouldMapRequestToEntityCorrectly() {
        // Act
        Technician result = technicianService.mapToEntity ( technicianRequest );

        // Assert
        assertNotNull ( result );
        assertEquals ( "tech@example.com", result.getEmailId ( ) );
        assertEquals ( "John", result.getFirstName ( ) );
        assertEquals ( "+1234567890", result.getMobileNumber ( ) );
        assertEquals ( "784-1234-5678901-1", result.getEmiratesId ( ) );
        assertEquals ( Status.ACTIVE, result.getStatus ( ) );
        assertEquals ( EMPLOYEE_ROLE.SUPERVISOR, result.getEmployeeRole ( ) );
    }
}