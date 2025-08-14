package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.request.AvailableServiceRequest;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableServiceManagementService {


	private final AvailableServiceRepository availableServiceRepository;

	private AvailableService mapToEntity(AvailableServiceRequest serviceRequest) {
	log.info("serviceRequest: {}", serviceRequest.getDescription ());
		return AvailableService
				.builder()
				.name(serviceRequest.getName())
				.description(serviceRequest.getDescription())
				.price(serviceRequest.getPrice())
				.laborPrice(serviceRequest.getLaborPrice())
				.build();
	}

	/**
	 * Creates a new available service.
	 *
	 * @param service the request object containing service details
	 * @return the created AvailableService entity
	 */
	@Transactional
	public AvailableService createService(AvailableServiceRequest service) {

		AvailableService save = availableServiceRepository.save ( mapToEntity ( service ) );
		log.info("Service saved with ID: {}", save.getId());
		log.info("Service saved with name: {}", save.getName());
		log.info("Service saved with description: {}", save.getDescription());
		return save;
	}

	/**
	 * Retrieves all available services.
	 *
	 * @return a list of all AvailableService entities
	 */
	@Transactional
	public List<AvailableService> getAllServices() {
		return availableServiceRepository.findAll();
	}

	/**
	 * Retrieves a service by its ID.
	 *
	 * @param id the ID of the service
	 * @return an Optional containing the AvailableService entity if found, or empty
	 *         if not found
	 */
	@Transactional
	public Optional<AvailableService> getServiceById(Integer id) {
		return availableServiceRepository.findById(id);
	}

	/**
	 * Updates an existing service.
	 *
	 * @param id             the ID of the service to update
	 * @param updatedService the request object containing updated service details
	 * @return the updated AvailableService entity
	 */
	@Transactional
	public AvailableService updateService(Integer id, AvailableService updatedService) {
		return availableServiceRepository.findById(id).map(existing -> {
			existing.setName(updatedService.getName());
			existing.setDescription(updatedService.getDescription());
			existing.setPrice(updatedService.getPrice());
			existing.setLaborPrice(updatedService.getLaborPrice());
			return availableServiceRepository.save(existing);
		}).orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
	}

	/**
	 * Deletes a service by its ID.
	 *
	 * @param id the ID of the service to delete
	 */
	@Transactional
	public void deleteService(Integer id) {
		availableServiceRepository.deleteById(id);
	}

	/**
	 * Searches for services by name.
	 *
	 * @param name the name to search for
	 * @return a list of AvailableService entities matching the search criteria
	 */
	@Transactional
	public List<AvailableService> searchByName(String name) {
		return availableServiceRepository.findByNameContainingIgnoreCase(name);
	}

	/**
	 * Searches for services by description.
	 *
	 * @param description the description to search for
	 * @return a list of AvailableService entities matching the search criteria
	 */
	@Transactional
	public List<AvailableService> searchByDescription(String description) {
		return availableServiceRepository.findByDescriptionContainingIgnoreCase(description);
	}

	/**
	 * Searches for services within a specified price range.
	 *
	 * @param min the minimum price
	 * @param max the maximum price
	 * @return a list of AvailableService entities within the specified price range
	 */
	@Transactional
	public List<AvailableService> searchByPriceRange(Double min, Double max) {
		return availableServiceRepository.findByPriceBetween(min, max);
	}
}
