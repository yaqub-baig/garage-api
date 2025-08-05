package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.TechnicianRequest;
import org.springframework.stereotype.Service;

@Service
public class TechnicianService {
	@Autowired
	private TechnicianRepository technicianRepository;

	@Autowired
	private GarageRepository garageRepo;

	/**
	 * Retrieves a list of technicians associated with a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return a list of technicians for the specified garage
	 */
//	@Transactional
//	public List<Technician> getTechniciansByGarage(Integer garageId) {
//		return technicianRepository.findByGarageId(garageId);
//	}

	Technician mapToEntity(TechnicianRequest technicianRequest) {
		return Technician.builder().emiratesId(technicianRequest.getEmiratesId())
				.firstName(technicianRequest.getFirstName()).lastName(technicianRequest.getLastName())
				.emailId(technicianRequest.getEmailId()).mobileNumber(technicianRequest.getMobileNumber())
				.vatNumber(technicianRequest.getVatNumber()).skillSet(technicianRequest.getSkillSet())
				.status(technicianRequest.getStatus()).build();
	}

	/**
	 * Onboards a new technician to a garage.
	 *
	 * @param technician the technician request containing details for onboarding
	 * @return the saved Technician entity
	 */
	@Transactional
	public Technician onboardTechnician(TechnicianRequest technician) {
		Garage garage = garageRepo.findById(technician.getGarageId())
				.orElseThrow(() -> new RuntimeException("Garage not found"));

		Technician garageTechnician = mapToEntity(technician);
		return technicianRepository.save(garageTechnician);
	}

	/**
	 * Finds a technician by their email address.
	 *
	 * @param email the email address of the technician
	 * @return an Optional containing the Technician if found, or empty if not found
	 */
	@Transactional
	public Optional<Technician> findByEmail(String email) {
		return technicianRepository.findByEmailId (email);
	}

	/**
	 * Deletes a technician by their ID.
	 *
	 * @param id the ID of the technician to be deleted
	 */
	@Transactional
	public void deleteTechnician(Integer id) {
		technicianRepository.deleteById(id);
	}

	/**
	 * Finds a technician by their ID.
	 *
	 * @param id the ID of the technician
	 * @return an Optional containing the Technician if found, or empty if not found
	 */
	@Transactional
	public Optional<Technician> findById(Integer id) {
		return technicianRepository.findById(id);
	}

	/**
	 * Finds technicians by their skill set.
	 *
	 * @param skill the skill to search for
	 * @return a list of technicians with the specified skill
	 */
	@Transactional
	public List<Technician> findBySkillSet(String skill) {
		return technicianRepository.findBySkillSetContaining(skill);
	}

	/**
	 * Finds technicians by their status.
	 *
	 * @param status the status to search for
	 * @return a list of technicians with the specified status
	 */
	@Transactional
	public List<Technician> findByStatus(String status) {
		return technicianRepository.findByStatus(status);
	}

	/**
	 * Finds a technician by their phone number.
	 *
	 * @param phone the phone number of the technician
	 * @return an Optional containing the Technician if found, or empty if not found
	 */
	@Transactional
	public Optional<Technician> findByPhone(String phone) {
		return technicianRepository.findByMobileNumber (phone);
	}

	/**
	 * Updates an existing technician's details.
	 *
	 * @param technicianRequest the request containing updated technician details
	 * @return the updated Technician entity
	 */
	@Transactional
	public Technician updateTechnician(TechnicianRequest technicianRequest) {
		Technician garageTechnician = mapToEntity(technicianRequest);
		return technicianRepository.save(garageTechnician);
	}

	/**
	 * Retrieves all technicians.
	 *
	 * @return a list of all technicians
	 */
	@Transactional
	public List<Technician> getAllTechnicians() {
		return technicianRepository.findAll();
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
		return technicianRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Technician not found with id: " + id));
	}
}
