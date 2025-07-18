package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.repository.GarageServiceRepository;

@Service
public class GarageServiceManagementService {

	@Autowired
	private GarageServiceRepository garageServiceRepository;

	public GarageService createGarageService(GarageService service) {
		return garageServiceRepository.save(service);
	}

	public List<GarageService> getAllGarageServices() {
		return garageServiceRepository.findAll();
	}

	public Optional<GarageService> getGarageServiceById(Integer id) {
		return garageServiceRepository.findById(id);
	}

	public GarageService updateGarageService(Integer id, GarageService updated) {
		return garageServiceRepository.findById(id).map(existing -> {
			existing.setIsActive(updated.getIsActive());
			existing.setAvailableService(updated.getAvailableService());
			existing.setGarage(updated.getGarage());
			return garageServiceRepository.save(existing);
		}).orElseThrow(() -> new RuntimeException("Garage service not found with ID: " + id));
	}

	public void deleteGarageService(Integer id) {
		garageServiceRepository.deleteById(id);
	}

	public List<GarageService> getByGarageId(Integer garageId) {
		return garageServiceRepository.findByGarage_Id(garageId);
	}

	public List<GarageService> getByActiveStatus(Boolean isActive) {
		return garageServiceRepository.findByIsActive(isActive);
	}

	public List<GarageService> getByGarageIdAndActive(Integer garageId, Boolean isActive) {
		return garageServiceRepository.findByGarage_IdAndIsActive(garageId, isActive);
	}

	public List<GarageService> getByServiceId(Integer serviceId) {
		return garageServiceRepository.findByAvailableService_Id(serviceId);
	}

	public List<GarageService> getByGarageAndServiceId(Integer garageId, Integer serviceId) {
		return garageServiceRepository.findByGarage_IdAndAvailableService_Id(garageId, serviceId);
	}
}
