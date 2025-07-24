package com.autoadda.apis.garage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.service.GarageManagementService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/garages")
@RequiredArgsConstructor
public class GarageController {

	private final GarageManagementService garageService;

	@PostMapping
	public ResponseEntity<Garage> createGarage(@RequestBody GarageRequest garage) {
		return ResponseEntity.status(HttpStatus.CREATED).body(garageService.createGarage(garage));
	}

	@GetMapping
	public ResponseEntity<List<Garage>> getAllGarages() {
		return ResponseEntity.ok(garageService.getAllGarages());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Garage> getGarageById(@PathVariable Integer id) {
		return garageService.getGarageById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Garage> updateGarage(@PathVariable Integer id, @RequestBody GarageRequest garage) {
		return ResponseEntity.ok(garageService.updateGarage(id, garage));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteGarage(@PathVariable Integer id) {
		garageService.deleteGarage(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/legal-name")
	public ResponseEntity<List<Garage>> searchByLegalName(@RequestParam String name) {
		return ResponseEntity.ok(garageService.findGaragesByLegalName(name));
	}

	@GetMapping("/service/{serviceId}")
	public ResponseEntity<List<Garage>> getGaragesByService(@PathVariable Integer serviceId) {
		return ResponseEntity.ok(garageService.findGaragesOfferingAvailableService(serviceId));
	}
}
