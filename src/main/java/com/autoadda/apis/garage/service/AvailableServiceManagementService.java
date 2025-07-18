package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.request.AvailableServiceRequest;

@Service
public class AvailableServiceManagementService {

	@Autowired
	private AvailableServiceRepository availableServiceRepository;

	private AvailableService mapToEntity(AvailableServiceRequest serviceRequest) {

		return AvailableService.builder().name(serviceRequest.getName()).description(serviceRequest.getDescription())
				.price(serviceRequest.getPrice()).laborPrice(serviceRequest.getLaborPrice()).build();
	}

	public AvailableService createService(AvailableServiceRequest service) {
		return availableServiceRepository.save(mapToEntity(service));
	}

	public List<AvailableService> getAllServices() {
		return availableServiceRepository.findAll();
	}

	public Optional<AvailableService> getServiceById(Integer id) {
		return availableServiceRepository.findById(id);
	}

	public AvailableService updateService(Integer id, AvailableService updatedService) {
		return availableServiceRepository.findById(id).map(existing -> {
			existing.setName(updatedService.getName());
			existing.setDescription(updatedService.getDescription());
			existing.setPrice(updatedService.getPrice());
			existing.setLaborPrice(updatedService.getLaborPrice());
			return availableServiceRepository.save(existing);
		}).orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));
	}

	public void deleteService(Integer id) {
		availableServiceRepository.deleteById(id);
	}

	public List<AvailableService> searchByName(String name) {
		return availableServiceRepository.findByNameContainingIgnoreCase(name);
	}

	public List<AvailableService> searchByDescription(String description) {
		return availableServiceRepository.findByDescriptionContainingIgnoreCase(description);
	}

	public List<AvailableService> searchByPriceRange(Double min, Double max) {
		return availableServiceRepository.findByPriceBetween(min, max);
	}
}
