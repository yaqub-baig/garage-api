package com.autoadda.apis.garage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.TechnicianRequest;

public class TechnicianService {
	@Autowired
	private TechnicianRepository technicianRepository;

	@Autowired
	private GarageRepository garageRepo;

	public List<Technician> getTechniciansByGarage(Integer garageId) {
		return technicianRepository.findByGarageId(garageId);
	}

	Technician mapToEntity(TechnicianRequest technicianRequest) {
		return Technician.builder().emiratesId(technicianRequest.getEmiratesId())
				.firstName(technicianRequest.getFirstName()).lastName(technicianRequest.getLastName())
				.emailId(technicianRequest.getEmailId()).mobileNumber(technicianRequest.getMobileNumber())
				.vatNumber(technicianRequest.getVatNumber()).skillSet(technicianRequest.getSkillSet())
				.status(technicianRequest.getStatus()).build();
	}

	public Technician onboardTechnician(TechnicianRequest technician) {
		Garage garage = garageRepo.findById(technician.getGarageId())
				.orElseThrow(() -> new RuntimeException("Garage not found"));

		Technician garageTechnician = mapToEntity(technician);
		return technicianRepository.save(garageTechnician);
	}

	public Optional<Technician> findByEmail(String email) {
		return technicianRepository.findByEmail(email);
	}

	public void deleteTechnician(Integer id) {
		technicianRepository.deleteById(id);
	}

	public Optional<Technician> findById(Integer id) {
		return technicianRepository.findById(id);
	}

	public List<Technician> findBySkillSet(String skill) {
		return technicianRepository.findBySkillSetContaining(skill);
	}

	public List<Technician> findByStatus(String status) {
		return technicianRepository.findByStatus(status);
	}

	public Optional<Technician> findByPhone(String phone) {
		return technicianRepository.findByPhone(phone);
	}

	public Technician updateTechnician(TechnicianRequest technicianRequest) {
		Technician garageTechnician = mapToEntity(technicianRequest);
		return technicianRepository.save(garageTechnician);
	}

	public List<Technician> getAllTechnicians() {
		return technicianRepository.findAll();
	}

	public Technician getTechnicianById(Integer id) {
		return technicianRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Technician not found with id: " + id));
	}
}
