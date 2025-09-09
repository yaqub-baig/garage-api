package com.autoadda.apis.garage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.garage.entity.PickupDropRequest;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.Status;

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
