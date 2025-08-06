package com.service.garage.apis.service;

import com.service.garage.apis.entity.GarageService;
import com.service.garage.apis.repository.GarageServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GarageServiceManagementService {


    private final GarageServiceRepository garageServiceRepository;

    /**
     * Creates a new GarageService entry in the database.
     *
     * @param service The GarageService object to be created.
     * @return The created GarageService object.
     */
    @Transactional
    public GarageService createGarageService(GarageService service) {
        return garageServiceRepository.save ( service );
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
    public GarageService updateGarageService(Integer id, GarageService updated) {
        return garageServiceRepository.findById ( id ).map ( existing -> {
            existing.setIsActive ( updated.getIsActive ( ) );
            existing.setAvailableService ( updated.getAvailableService ( ) );
            existing.setGarage ( updated.getGarage ( ) );
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
    public List<GarageService> getByGarageId(Integer garageId) {
        return garageServiceRepository.findByGarage_Id ( garageId );
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
