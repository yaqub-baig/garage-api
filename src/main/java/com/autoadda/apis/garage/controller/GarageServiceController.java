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

import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.service.GarageServiceManagementService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/garage-services")
public class GarageServiceController {

	@Autowired
	private GarageServiceManagementService garageServiceService;

	@PostMapping
	public ResponseEntity<GarageService> create(@RequestBody GarageService garageService) {
		return ResponseEntity.ok(garageServiceService.createGarageService(garageService));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Optional<GarageService>> getById(@PathVariable Integer id) {
		return ResponseEntity.ok(garageServiceService.getGarageServiceById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<GarageService> update(@PathVariable Integer id, @RequestBody GarageService updated) {
		return ResponseEntity.ok(garageServiceService.updateGarageService(id, updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		garageServiceService.deleteGarageService(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<GarageService>> getAll() {
		return ResponseEntity.ok(garageServiceService.getAllGarageServices());
	}

	@GetMapping("/garage/{garageId}")
	public ResponseEntity<List<GarageService>> getByGarage(@PathVariable Integer garageId) {
		return ResponseEntity.ok(garageServiceService.getByGarageId(garageId));
	}
}
