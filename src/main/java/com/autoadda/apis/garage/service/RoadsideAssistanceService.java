package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.RoadsideAssistance;
import com.autoadda.apis.garage.repository.RoadsideAssistanceRepository;
import com.autoadda.apis.garage.util.Status;

import jakarta.transaction.Transactional;

@Service
public class RoadsideAssistanceService {

	@Autowired
	private RoadsideAssistanceRepository assistanceRepository;

	/**
	 * Creates a new roadside assistance request.
	 *
	 * @param request the roadside assistance request to create
	 * @return the created roadside assistance request
	 */
	@Transactional
	public RoadsideAssistance createRequest(RoadsideAssistance request) {
		request.setStatus(Status.REQUESTED);
		request.setRequestTime(LocalDateTime.now());
		return assistanceRepository.save(request);
	}

	/**
	 * Retrieves all roadside assistance requests.
	 *
	 * @return a list of all roadside assistance requests
	 */
	@Transactional
	public List<RoadsideAssistance> getRequestsByOwner(Integer ownerId) {
		return assistanceRepository.findByVehicleOwnerId(ownerId);
	}

	/**
	 * Updates the status of a roadside assistance request.
	 *
	 * @param requestId the ID of the request to update
	 * @param status    the new status to set
	 * @param remarks   any remarks associated with the status update
	 * @return the updated roadside assistance request
	 */
	@Transactional
	public RoadsideAssistance updateStatus(Integer requestId, Status status, String remarks) {
		RoadsideAssistance request = assistanceRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));
		request.setStatus(status);
		if (status == Status.COMPLETED) {
			request.setCompletedAt(LocalDateTime.now());
		}
		request.setRemarks(remarks);
		return assistanceRepository.save(request);
	}

	/**
	 * Retrieves a roadside assistance request by its ID.
	 *
	 * @param id the ID of the request to retrieve
	 * @return an Optional containing the roadside assistance request if found, or
	 *         empty if not found
	 */
	@Transactional
	public Optional<RoadsideAssistance> getRequestById(Integer id) {
		return assistanceRepository.findById(id);
	}
}
