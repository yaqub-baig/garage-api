package com.service.garage.apis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.CommunicationPreference;
import com.service.garage.apis.request.CommunicationPreferenceRequest;
import com.service.garage.apis.service.CommunicationPreferenceService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/preferences")
public class CommunicationPreferenceController {

	@Autowired
	private CommunicationPreferenceService service;

	@PostMapping
	public ResponseEntity<CommunicationPreference> setPreferences(
			@RequestBody CommunicationPreferenceRequest preference) {
		return ResponseEntity.ok(service.saveOrUpdatePreference(preference));
	}

	@GetMapping("/{customerId}")
	public ResponseEntity<CommunicationPreference> getPreferences(@PathVariable Integer customerId) {
		return service.getPreference(customerId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/{customerId}")
	public ResponseEntity<CommunicationPreference> getPreferencesByIdAndCustomerId(@PathVariable Integer id,
			@PathVariable Integer customerId) {
		return service.getPreferenceByIdAndCustomerId(id, customerId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/status/{customerId}/{status}")
	public ResponseEntity<CommunicationPreference> getPreferencesByCustomerIdAndStatus(@PathVariable Integer customerId,
			@PathVariable String status) {
		return service.getPreferenceByCustomerIdAndStatus(customerId, status).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deletePreference(@PathVariable Integer id) {
		service.deletePreference(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/update")
	public ResponseEntity<CommunicationPreference> updatePreference(
			@RequestBody CommunicationPreferenceRequest preference) {
		return ResponseEntity.ok(service.saveOrUpdatePreference(preference));
	}

	@GetMapping("/update/{id}/{customerId}")
	public ResponseEntity<CommunicationPreference> updatePreferenceByIdAndCustomerId(@PathVariable Integer id,
			@PathVariable Integer customerId) {
		return service.getPreferenceByIdAndCustomerId(id, customerId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
