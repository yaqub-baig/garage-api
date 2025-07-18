package com.autoadda.apis.garage.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.request.AvailableServiceRequest;
import com.autoadda.apis.garage.service.AvailableServiceManagementService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/available-services")
public class AvailableServiceController {

	@Autowired
	private AvailableServiceManagementService availableServiceService;

	@PostMapping
	public ResponseEntity<AvailableService> create(@RequestBody AvailableServiceRequest availableService) {
		return ResponseEntity.ok(availableServiceService.createService(availableService));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Optional<AvailableService>> getById(@PathVariable Integer id) {
		return ResponseEntity.ok(availableServiceService.getServiceById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<AvailableService> update(@PathVariable Integer id, @RequestBody AvailableService updated) {
		return ResponseEntity.ok(availableServiceService.updateService(id, updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		availableServiceService.deleteService(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<AvailableService>> getAll() {
		return ResponseEntity.ok(availableServiceService.getAllServices());
	}
}
