package com.service.garage.apis.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.Technician;
import com.service.garage.apis.entity.TechnicianAvailability;
import com.service.garage.apis.repository.TechnicianAvailabilityRepository;
import com.service.garage.apis.repository.TechnicianRepository;
import com.service.garage.apis.request.TechnicianAvailabilityRequest;

import jakarta.transaction.Transactional;

@Service
public class TechnicianAvailabilityService {

	@Autowired
	private TechnicianAvailabilityRepository availabilityRepo;

	@Autowired
	private TechnicianRepository technicianRepo;

	/**
	 * Maps a TechnicianAvailabilityRequest to a TechnicianAvailability
	 * entity.
	 *
	 * @param request the request object containing availability details
	 * @return the mapped TechnicianAvailability entity
	 */

	TechnicianAvailability mapToEntity(TechnicianAvailabilityRequest request) {
		TechnicianAvailability availability = new TechnicianAvailability();
		availability.setId(request.getId());
		availability.setDate(request.getDate());
		availability.setStartTime(request.getStartTime());
		availability.setEndTime(request.getEndTime());
		availability.setStatus(request.getStatus());

		Technician technician = technicianRepo.findById(request.getTechnicianId())
				.orElseThrow(() -> new RuntimeException("Technician not found"));
		availability.setTechnician(technician);

		return availability;
	}

	@Transactional
	public TechnicianAvailability addAvailability(TechnicianAvailabilityRequest request) {
		Technician technician = technicianRepo.findById(request.getTechnicianId())
				.orElseThrow(() -> new RuntimeException("Technician not found"));

		return availabilityRepo.save(mapToEntity(request));
	}

	@Transactional
	public List<TechnicianAvailability> getAvailabilityForDate(Integer technicianId, LocalDate date) {
		return availabilityRepo.findByTechnicianIdAndDate(technicianId, date);
	}

	@Transactional
	public List<TechnicianAvailability> getAllAvailabilityForTechnician(Integer technicianId) {
		return availabilityRepo.findByTechnicianId(technicianId);
	}

	@Transactional
	public List<TechnicianAvailability> getAllAvailabilityForDate(LocalDate date) {
		return availabilityRepo.findByDate(date);
	}

	@Transactional
	public List<TechnicianAvailability> getAllAvailability() {
		return availabilityRepo.findAll();
	}

	@Transactional
	public TechnicianAvailability updateAvailability(TechnicianAvailabilityRequest request) {
		TechnicianAvailability existingAvailability = availabilityRepo.findById(request.getId())
				.orElseThrow(() -> new RuntimeException("Availability not found"));

		existingAvailability.setDate(request.getDate());
		existingAvailability.setStartTime(request.getStartTime());
		existingAvailability.setEndTime(request.getEndTime());
		existingAvailability.setStatus(request.getStatus());

		Technician technician = technicianRepo.findById(request.getTechnicianId())
				.orElseThrow(() -> new RuntimeException("Technician not found"));
		existingAvailability.setTechnician(technician);

		return availabilityRepo.save(existingAvailability);
	}

	@Transactional
	public void deleteAvailability(Integer id) {
		TechnicianAvailability availability = availabilityRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Availability not found"));
		availabilityRepo.delete(availability);
	}

	@Transactional
	public void deleteAllAvailabilityForTechnician(Integer technicianId) {
		List<TechnicianAvailability> availabilities = availabilityRepo.findByTechnicianId(technicianId);
		if (availabilities.isEmpty()) {
			throw new RuntimeException("No availability found for technician with ID: " + technicianId);
		}
		availabilityRepo.deleteAll(availabilities);
	}

	@Transactional
	public void deleteAllAvailabilityForDate(LocalDate date) {
		List<TechnicianAvailability> availabilities = availabilityRepo.findByDate(date);
		if (availabilities.isEmpty()) {
			throw new RuntimeException("No availability found for date: " + date);
		}
		availabilityRepo.deleteAll(availabilities);
	}

	@Transactional
	public void deleteAllAvailability() {
		List<TechnicianAvailability> availabilities = availabilityRepo.findAll();
		if (availabilities.isEmpty()) {
			throw new RuntimeException("No availability found");
		}
		availabilityRepo.deleteAll(availabilities);
	}

	@Transactional
	public TechnicianAvailability getAvailabilityById(Integer id) {
		return availabilityRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Availability not found with ID: " + id));
	}

	@Transactional
	public List<TechnicianAvailability> getAvailabilityByTechnicianId(Integer technicianId) {
		return availabilityRepo.findByTechnicianId(technicianId);
	}

}
