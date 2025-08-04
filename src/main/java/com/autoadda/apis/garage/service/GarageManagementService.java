package com.autoadda.apis.garage.service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.PickupDropRequest;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.PickupDropRequestRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GarageManagementService {

	@Autowired
	private GarageRepository garageRepository;

	@Autowired
	private TechnicianRepository technicianRepository;

	@Autowired
	private PickupDropRequestRepository pickupDropRequestRepository;

	Garage mapToEntity(GarageRequest garageRequest) {
		Garage garage = new Garage();
		garage.setLegalName(garageRequest.getLegalName());
		garage.setGarageName(garageRequest.getGarageName());
		garage.setEmailId(garageRequest.getEmailId());
		garage.setContactNumber(garageRequest.getContactNumber());
		garage.setWebSite(garageRequest.getWebSite());
		garage.setAccountStatus(Status.PENDING); // Default status
		garage.setGarageType(garageRequest.getGarageType());
		garage.setChainId(garageRequest.getChainId());
		return garage;
	}

	// Create a new Garage
	@Transactional
	public Garage createGarage(GarageRequest garageRequest) {
		garageRequest.setCreatedAt(LocalDate.now());
		return garageRepository.save(mapToEntity(garageRequest));
	}

	// Get all garages
	@Transactional
	public List<Garage> getAllGarages() {
		return garageRepository.findAll();
	}

	// Get all garages with services
	@Transactional
	public List<Garage> getAllGaragesWithServices() {
		return garageRepository.findAllWithServices();
	}

	// Get garage by ID
	@Transactional
	public Optional<Garage> getGarageById(Integer id) {
		return Optional.of(garageRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Garage not found with id: " + id)));
	}

	// Get garage with services
	@Transactional
	public Garage getGarageWithServicesById(Integer id) {
		return garageRepository.findWithServicesById(id)
				.orElseThrow(() -> new NoSuchElementException("Garage not found with id: " + id));
	}

	// Update existing garage
	@Transactional
	public Garage updateGarage(Integer id, GarageRequest updatedGarage) {
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
	@Transactional
	public void deleteGarage(Integer id) {
		garageRepository.deleteById(id);
	}

	// Search/filter methods
	@Transactional
	public List<Garage> findGaragesByLegalName(String legalName) {
		return garageRepository.findByLegalNameContainingIgnoreCase(legalName);
	}

	@Transactional
	public List<Garage> findGaragesByGarageName(String name) {
		return garageRepository.findByGarageNameContainingIgnoreCase(name);
	}

	@Transactional
	public List<Garage> findGaragesByStatus(Status status) {
		return garageRepository.findByAccountStatus(status);
	}

	@Transactional
	public List<Garage> findGaragesByChainId(Integer chainId) {
		return garageRepository.findByChainId(chainId);
	}

	@Transactional
	public List<Garage> findGaragesByType(String type) {
		return garageRepository.findByGarageType(type);
	}

	@Transactional
	public Optional<Garage> findGarageByEmail(String email) {
		return garageRepository.findByEmailId(email);
	}

	@Transactional
	public Optional<Garage> findGarageByContact(String contact) {
		return garageRepository.findByContactNumber(contact);
	}

	@Transactional
	public Optional<Garage> findGarageByWebsite(String website) {
		return garageRepository.findByWebSite(website);
	}

	@Transactional
	public Optional<Garage> findGarageByGarageNameExact(String garageName) {
		return garageRepository.findByGarageName(garageName);
	}

	@Transactional
	public List<Garage> findGaragesOfferingAvailableService(Integer serviceId) {
		return garageRepository.findGaragesByAvailableServiceId(serviceId);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndStatus(Integer id, Status status) {
		return garageRepository.findByIdAndAccountStatus(id, status);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndChainId(Integer id, Integer chainId) {
		return garageRepository.findByIdAndChainId(id, chainId);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndGarageType(Integer id, String garageType) {
		return garageRepository.findByIdAndGarageType(id, garageType);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndLegalName(Integer id, String legalName) {
		return garageRepository.findByIdAndLegalName(id, legalName);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndWebsite(Integer id, String website) {
		return garageRepository.findByIdAndWebSite(id, website);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndEmail(Integer id, String email) {
		return garageRepository.findByIdAndEmailId(id, email);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndContact(Integer id, String contactNumber) {
		return garageRepository.findByIdAndContactNumber(id, contactNumber);
	}

	@Transactional
	public Optional<Garage> findGarageByIdAndGarageName(Integer id, String name) {
		return garageRepository.findByIdAndGarageName(id, name);
	}

	@Transactional
	public Optional<Garage> findGarageByIdStatusChain(Integer id, Status status, Integer chainId) {
		return garageRepository.findByIdAndAccountStatusAndChainId(id, status, chainId);
	}

	@Transactional
	public Optional<Garage> findGarageByIdStatusType(Integer id, Status status, String type) {
		return garageRepository.findByIdAndAccountStatusAndGarageType(id, status, type);
	}

	@Transactional
	public Optional<Garage> findGarageByIdStatusLegalName(Integer id, Status status, String legalName) {
		return garageRepository.findByIdAndAccountStatusAndLegalName(id, status, legalName);
	}

	@Transactional
	public Technician onboardTechnician(Integer garageId, Technician technician, Integer ownerId)
			throws AccessDeniedException {
		Garage garage = garageRepository.findById(garageId)
				.orElseThrow(() -> new EntityNotFoundException("Garage not found"));

		if (!garage.getGarageOwner().getId().equals(ownerId)) {
			throw new AccessDeniedException("Only garage owner can onboard technicians");
		}

		// You may check for duplicate email, phone etc.
		technician.setStatus(Status.ACTIVE);
		technician.setGarage(garage);

		return technicianRepository.save(technician);
	}

	@Transactional
	public PickupDropRequest createPickupDropRequest(PickupDropRequest request) {
		request.setStatus(Status.REQUESTED);
		return pickupDropRequestRepository.save(request);
	}

	@Transactional
	public PickupDropRequest assignDriver(Integer requestId, Integer driverId) {
		PickupDropRequest request = pickupDropRequestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));
		request.setAssignedDriverId(driverId);
		request.setStatus(Status.SCHEDULED);
		return pickupDropRequestRepository.save(request);
	}

	@Transactional
	public PickupDropRequest updatePickupDropStatus(Integer requestId, Status status) {
		PickupDropRequest request = pickupDropRequestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));
		request.setStatus(status);
		return pickupDropRequestRepository.save(request);
	}

}
