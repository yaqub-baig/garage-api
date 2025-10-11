package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageServiceMapper;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.request.GarageServiceUpdateRequest;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GarageServiceManagementService {
    private final GarageRepository garageRepository;
    private final AvailableServiceRepository availableServiceRepository;
    private final GarageServiceRepository garageServiceRepository;
    private final GarageServiceMapper garageServiceMapper;

    private GarageService mapToEntity(GarageServiceRequest service) {
        GarageService garageService = new GarageService ( );
        garageService.setIsActive ( service.isActive ( ) );
        garageService.setGarage ( garageRepository.findById ( service.getGarageId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) ) );
        garageService.setAvailableService ( availableServiceRepository.findById ( service.getServiceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" ) ) );
        garageService.setLabourPrice ( garageService.getAvailableService ( ).getLaborPrice ( ) );
        garageService.setPrice ( garageService.getAvailableService().getPrice ( ) );
        return garageService;
    }

    /**
     * Creates a new GarageService entry in the database.
     *
     * @param service The GarageService object to be created.
     * @return The created GarageService object.
     * `
     */
    @Transactional
    public GarageService createGarageService(GarageServiceRequest service) {
        return garageServiceRepository.save ( mapToEntity ( service ) );
    }

    /**
     * Retrieves all GarageService entries from the database.
     *
     * @return A list of all GarageService objects.
     */
    @Transactional
    public List<GarageService> getAllGarageServices() {
        return garageServiceRepository.findAll ( );
    }

    /**
     * Retrieves a GarageService entry by its ID.
     *
     * @param id The ID of the GarageService to retrieve.
     * @return An Optional containing the GarageService if found, or empty if not
     * found.
     */
    @Transactional
    public Optional<GarageService> getGarageServiceById(Integer id) {
        return garageServiceRepository.findById ( id );
    }

    /**
     * Updates an existing GarageService entry.
     *
     * @param id      The ID of the GarageService to update.
     * @param updated The updated GarageService object.
     * @return The updated GarageService object.
     */
    @Transactional
    public GarageService updateGarageService(Integer id, GarageServiceUpdateRequest updated) {
        return garageServiceRepository.findById ( id ).map ( existing -> {
            existing.setIsActive ( updated.getIsActive ( ) );
            existing.setLabourPrice ( updated.getLabourPrice ( ) );
            existing.setPrice ( updated.getPrice ( ) );
            return garageServiceRepository.save ( existing );
        } ).orElseThrow ( () -> new RuntimeException ( "Garage service not found with ID: " + id ) );
    }

    /**
     * Deletes a GarageService entry by its ID.
     *
     * @param id The ID of the GarageService to delete.
     */
    @Transactional
    public void deleteGarageService(Integer id) {
        garageServiceRepository.deleteById ( id );
    }

    /**
     * Retrieves GarageService entries by garage ID.
     *
     * @param garageId The ID of the garage.
     * @return A list of GarageService objects associated with the specified garage
     * ID.
     */
    @Transactional
    public List<GarageServiceResponse> getByGarageId(Integer garageId) {
        return garageServiceRepository.findByGarage_Id ( garageId ).stream ( ).map ( garageServiceMapper::entityToResponse ).toList ( );
    }

    /**
     * Retrieves GarageService entries by active status.
     *
     * @param isActive The active status to filter by.
     * @return A list of GarageService objects with the specified active status.
     */
    @Transactional
    public List<GarageService> getByActiveStatus(Boolean isActive) {
        return garageServiceRepository.findByIsActive ( isActive );
    }

    /**
     * Retrieves GarageService entries by garage ID and active status.
     *
     * @param garageId The ID of the garage.
     * @param isActive The active status to filter by.
     * @return A list of GarageService objects associated with the specified garage
     * ID and active status.
     */
    @Transactional
    public List<GarageService> getByGarageIdAndActive(Integer garageId, Boolean isActive) {
        return garageServiceRepository.findByGarage_IdAndIsActive ( garageId, isActive );
    }

    /**
     * Retrieves GarageService entries by service ID.
     *
     * @param serviceId The ID of the service.
     * @return A list of GarageService objects associated with the specified service
     * ID.
     */
    @Transactional
    public List<GarageService> getByServiceId(Integer serviceId) {
        return garageServiceRepository.findByAvailableService_Id ( serviceId );
    }

    /**
     * Retrieves GarageService entries by garage ID and service ID.
     *
     * @param garageId  The ID of the garage.
     * @param serviceId The ID of the service.
     * @return A list of GarageService objects associated with the specified garage
     * ID and service ID.
     */
    @Transactional
    public List<GarageService> getByGarageAndServiceId(Integer garageId, Integer serviceId) {
        return garageServiceRepository.findByGarage_IdAndAvailableService_Id ( garageId, serviceId );
    }
}
