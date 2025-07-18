package com.autoadda.apis.garage.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.util.Status;

@Service
public class GarageManagementService {

	@Autowired
	private GarageRepository garageRepository;

	// Create a new Garage
	public Garage createGarage(Garage garage) {
		garage.setInsertTms(LocalDate.now());
		return garageRepository.save(garage);
	}

	// Get all garages
	public List<Garage> getAllGarages() {
		return garageRepository.findAll();
	}

	// Get all garages with services
	public List<Garage> getAllGaragesWithServices() {
		return garageRepository.findAllWithServices();
	}

	// Get garage by ID
	public Optional<Garage> getGarageById(Integer id) {
		return Optional.of(garageRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Garage not found with id: " + id)));
	}

	// Get garage with services
	public Garage getGarageWithServicesById(Integer id) {
		return garageRepository.findWithServicesById(id)
				.orElseThrow(() -> new NoSuchElementException("Garage not found with id: " + id));
	}

	// Update existing garage
	public Garage updateGarage(Integer id, Garage updatedGarage) {
		Garage existing = garageRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Garage not found with id: " + id));
		existing.setLegalName(updatedGarage.getLegalName());
		existing.setGarageName(updatedGarage.getGarageName());
		existing.setEmailId(updatedGarage.getEmailId());
		existing.setContactNumber(updatedGarage.getContactNumber());
		existing.setWebSite(updatedGarage.getWebSite());
		existing.setAccountStatus(updatedGarage.getAccountStatus());
		existing.setGarageType(updatedGarage.getGarageType());
		existing.setChainId(updatedGarage.getChainId());
		return garageRepository.save(existing);
	}

	// Delete garage
	public void deleteGarage(Integer id) {
		garageRepository.deleteById(id);
	}

	// Search/filter methods
	public List<Garage> findGaragesByLegalName(String legalName) {
		return garageRepository.findByLegalNameContainingIgnoreCase(legalName);
	}

	public List<Garage> findGaragesByGarageName(String name) {
		return garageRepository.findByGarageNameContainingIgnoreCase(name);
	}

	public List<Garage> findGaragesByStatus(Status status) {
		return garageRepository.findByAccountStatus(status);
	}

	public List<Garage> findGaragesByChainId(Integer chainId) {
		return garageRepository.findByChainId(chainId);
	}

	public List<Garage> findGaragesByType(String type) {
		return garageRepository.findByGarageType(type);
	}

	public Optional<Garage> findGarageByEmail(String email) {
		return garageRepository.findByEmailId(email);
	}

	public Optional<Garage> findGarageByContact(String contact) {
		return garageRepository.findByContactNumber(contact);
	}

	public Optional<Garage> findGarageByWebsite(String website) {
		return garageRepository.findByWebSite(website);
	}

	public Optional<Garage> findGarageByGarageNameExact(String garageName) {
		return garageRepository.findByGarageName(garageName);
	}

	public List<Garage> findGaragesOfferingAvailableService(Integer serviceId) {
		return garageRepository.findGaragesByAvailableServiceId(serviceId);
	}

	public Optional<Garage> findGarageByIdAndStatus(Integer id, Status status) {
		return garageRepository.findByIdAndAccountStatus(id, status);
	}

	public Optional<Garage> findGarageByIdAndChainId(Integer id, Integer chainId) {
		return garageRepository.findByIdAndChainId(id, chainId);
	}

	public Optional<Garage> findGarageByIdAndGarageType(Integer id, String garageType) {
		return garageRepository.findByIdAndGarageType(id, garageType);
	}

	public Optional<Garage> findGarageByIdAndLegalName(Integer id, String legalName) {
		return garageRepository.findByIdAndLegalName(id, legalName);
	}

	public Optional<Garage> findGarageByIdAndWebsite(Integer id, String website) {
		return garageRepository.findByIdAndWebSite(id, website);
	}

	public Optional<Garage> findGarageByIdAndEmail(Integer id, String email) {
		return garageRepository.findByIdAndEmailId(id, email);
	}

	public Optional<Garage> findGarageByIdAndContact(Integer id, String contactNumber) {
		return garageRepository.findByIdAndContactNumber(id, contactNumber);
	}

	public Optional<Garage> findGarageByIdAndGarageName(Integer id, String name) {
		return garageRepository.findByIdAndGarageName(id, name);
	}

	public Optional<Garage> findGarageByIdStatusChain(Integer id, Status status, Integer chainId) {
		return garageRepository.findByIdAndAccountStatusAndChainId(id, status, chainId);
	}

	public Optional<Garage> findGarageByIdStatusType(Integer id, Status status, String type) {
		return garageRepository.findByIdAndAccountStatusAndGarageType(id, status, type);
	}

	public Optional<Garage> findGarageByIdStatusLegalName(Integer id, Status status, String legalName) {
		return garageRepository.findByIdAndAccountStatusAndLegalName(id, status, legalName);
	}
}
