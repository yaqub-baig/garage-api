package com.service.garage.apis.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.service.garage.apis.entity.AvailableService;
import com.service.garage.apis.request.AvailableServiceRequest;
import com.service.garage.apis.service.AvailableServiceManagementService;



@RestController
@RequestMapping("/available-services")
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
