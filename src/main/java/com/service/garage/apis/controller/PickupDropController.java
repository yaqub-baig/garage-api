package com.service.garage.apis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.PickupDropRequest;
import com.service.garage.apis.service.GarageManagementService;
import com.service.garage.apis.util.Status;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pickup-drop")
@RequiredArgsConstructor
public class PickupDropController {

	private final GarageManagementService garageManagementService;

	@PostMapping
	public ResponseEntity<PickupDropRequest> createRequest(@RequestBody PickupDropRequest request) {
		return ResponseEntity.ok(garageManagementService.createPickupDropRequest(request));
	}

	@PutMapping("/{id}/assign-driver/{driverId}")
	public ResponseEntity<PickupDropRequest> assignDriver(@PathVariable Integer id, @PathVariable Integer driverId) {
		return ResponseEntity.ok(garageManagementService.assignDriver(id, driverId));
	}

	@PutMapping("/{id}/status/{status}")
	public ResponseEntity<PickupDropRequest> updateStatus(@PathVariable Integer id, @PathVariable Status status) {
		return ResponseEntity.ok(garageManagementService.updatePickupDropStatus(id, status));
	}
}
