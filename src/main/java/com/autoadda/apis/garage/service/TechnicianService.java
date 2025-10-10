package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.TechnicianMapper;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.TechnicianResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.ERole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {
    private final TechnicianMapper technicianMapper;
    private final TechnicianRepository technicianRepository;
    private final GarageRepository garageRepo;
    private final AvailableServiceRepository availableServiceRepository;
    private final AuthClient authClient;

    /**
     * Retrieves a list of technicians associated with a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of technicians for the specified garage
     */
    @Transactional
    public List<TechnicianResponse> getTechniciansByGarage(Integer garageId) {
        return technicianRepository.findByGarageId ( garageId ).stream ( ).filter ( t -> ! t.getIsDeleted ( ) ).map ( technicianMapper::entityToResponse ).toList ( );
    }

    Technician mapToEntity(TechnicianRequest technicianRequest) {

        return Technician
                .builder ( )
                .emailId ( technicianRequest.getEmailId ( ) )
                .vatNumber ( technicianRequest.getVatNumber ( ) )
                .firstName ( technicianRequest.getFirstName ( ) )
                .emiratesId ( technicianRequest.getEmiratesId ( ) )
                .mobileNumber ( technicianRequest.getMobileNumber ( ) )
                .status ( technicianRequest.getStatus ( ) )
                .employeeRole ( technicianRequest.getEmployeeRole ( ) )
                .build ( );
    }

    /**
     * Onboards a new technician to a garage.
     *
     * @param technician the technician request containing details for onboarding
     * @return the saved Technician entity
     */
    @Transactional
    public TechnicianResponse onboardTechnician(TechnicianRequest technician) {
        // Check if technician with the same mobile number already exists

        Garage garage = garageRepo.findById ( technician.getGarageId ( ) )

                .orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );

        Technician garageTechnician = mapToEntity ( technician );
        Optional<Technician> existingTechnician = technicianRepository.findByMobileNumber ( technician.getMobileNumber ( ) );
        if ( existingTechnician.isPresent ( ) && ! existingTechnician.get ( ).getIsDeleted ( ) ) {
            throw new AlreadyExistsException ( "Technician with this phone number already exists" );
        }
        if ( existingTechnician.isPresent ( ) && existingTechnician.get ( ).getIsDeleted ( ) ) {
            existingTechnician.get ( ).setIsDeleted ( false );
            garageTechnician = existingTechnician.get ( );
        }
        garageTechnician.setFirstName ( technician.getFirstName ( ) );
        garageTechnician.setEmailId ( technician.getEmailId ( ) );
        garageTechnician.setGarage ( garage );
        garageTechnician.setSkillSet ( availableServiceRepository.findAllById ( technician.getSkillSetIds ( ) ) );

        ResponseEntity<UserResponse> userDetails = authClient.getUserDetails ( garageTechnician.getMobileNumber ( ) );
        if ( userDetails == null || ! userDetails.getStatusCode ( ).is2xxSuccessful ( ) || userDetails.getBody ( ) == null ) {
            log.error ( "User not found in auth service with phone number: {}", garageTechnician.getMobileNumber ( ) );
            throw new ResourceNotFoundException ( "User not found in auth service with phone number: " + garageTechnician.getMobileNumber ( ) );
        }
        if ( garage.getGarageOwner ( ).equals ( userDetails.getBody ( ).getId ( ) ) ) {
            throw new ResourceNotFoundException ( "Garage owner cannot be added as technician" );
        }
        garageTechnician.setAutoAddaId ( userDetails.getBody ( ).getId ( ) );
        authClient.updateRole ( garageTechnician.getAutoAddaId ( ), ERole.GARAGE_TECH );

        return technicianMapper.entityToResponse ( technicianRepository.saveAndFlush ( garageTechnician ) );
    }

    /**
     * Finds a technician by their email address.
     *
     * @param email the email address of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<TechnicianResponse> findByEmail(String email) {
        return technicianRepository.findByEmailId ( email ).map ( technicianMapper::entityToResponse );
    }

    /**
     * Deletes a technician by their ID.
     *
     * @param id the ID of the technician to be deleted
     */
    @Transactional
    public void deleteTechnician(Integer id) {
        Technician technician = technicianRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "technician not found" ) );
        technician.setIsDeleted ( true );
        technicianRepository.save ( technician );
    }

    /**
     * Finds a technician by their ID.
     *
     * @param id the ID of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<TechnicianResponse> findById(Integer id) {
        return technicianRepository.findById ( id )
                .map ( technicianMapper::entityToResponse );
    }

    /**
     * Finds technicians by their skill set.
     *
     * @param skill the skill to search for
     * @return a list of technicians with the specified skill
     */
    @Transactional
    public List<TechnicianResponse> findBySkillSet(Integer skillId) {
        return technicianRepository.findBySkillSetId ( skillId ).stream ( ).filter ( t -> ! t.getIsDeleted ( ) ).map ( technicianMapper::entityToResponse ).toList ( );
    }

    /**
     * Finds technicians by their status.
     *
     * @param status the status to search for
     * @return a list of technicians with the specified status
     */
    @Transactional
    public List<TechnicianResponse> findByStatus(String status) {
        return technicianRepository
                .findByStatus ( status )
                .stream ( )
                .filter ( t -> ! t.getIsDeleted ( ) )
                .map ( technicianMapper::entityToResponse ).toList ( );
    }

    /**
     * Finds a technician by their phone number.
     *
     * @param phone the phone number of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<Technician> findByPhone(String phone) {
        return technicianRepository.findByMobileNumber ( phone );
    }

    /**
     * Updates an existing technician's details.
     *
     * @param id
     * @param technicianRequest the request containing updated technician details
     * @return the updated Technician entity
     */
    @Transactional
    public TechnicianResponse updateTechnician(Integer id, TechnicianRequest technicianRequest) {
        Technician technician = technicianRepository.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Technician not found with id: " + id ) );
        Technician garageTechnician = technicianMapper.updateTechnician ( technician, technicianRequest );
        if ( technicianRequest.getSkillSetIds ( ) != null && ! technicianRequest.getSkillSetIds ( ).isEmpty ( ) ) {
            log.info ( "Updating 1 skill set for technician id {}: {}", id, garageTechnician.getSkillSet ( ).stream ( ).map ( AvailableService::getId ).toList ( ) );
            garageTechnician.getSkillSet ( ).clear ( );
            log.info ( "Updating 2 skill set for technician id {}: {}", id, garageTechnician.getSkillSet ( ).stream ( ).map ( AvailableService::getId ).toList ( ) );
            garageTechnician.getSkillSet ( ).addAll ( availableServiceRepository.findAllById ( technicianRequest.getSkillSetIds ( ) ) );
            log.info ( "Updating 3 skill set for technician id {}: {}", id, garageTechnician.getSkillSet ( ).stream ( ).map ( AvailableService::getId ).toList ( ) );
        }
        return technicianMapper.entityToResponse ( technicianRepository.save ( garageTechnician ) );
    }

    /**
     * Retrieves all technicians.
     *
     * @return a list of all technicians
     */
    @Transactional
    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll ( );
    }

    /**
     * Retrieves a technician by their ID.
     *
     * @param id the ID of the technician
     * @return the Technician entity if found
     * @throws RuntimeException if the technician is not found
     */
    @Transactional
    public Technician getTechnicianById(Integer id) {
        return technicianRepository.findById ( id )
                .orElseThrow ( () -> new RuntimeException ( "Technician not found with id: " + id ) );
    }

    public Optional<TechnicianResponse> findByAutoAddaId(Integer autoAddaId) {
        return technicianRepository.findByAutoAddaId ( autoAddaId )
                .map ( technicianMapper::entityToResponse );
    }
}
