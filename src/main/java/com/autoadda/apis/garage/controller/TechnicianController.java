package com.autoadda.apis.garage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.service.TechnicianService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

	@Autowired
	private TechnicianService technicianService;

	@PostMapping
	public ResponseEntity<Technician> addTechnician(@RequestBody TechnicianRequest request) {
		Technician created = technicianService.onboardTechnician(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

//	@GetMapping("/garage/{garageId}")
//	public ResponseEntity<List<Technician>> getByGarage(@PathVariable Integer garageId) {
//		return ResponseEntity.ok(technicianService.getTechniciansByGarage(garageId));
//	}
	
	@GetMapping("/email/{email}")
	public ResponseEntity<Technician> getByEmail(@PathVariable String email) {
		return technicianService.findByEmail(email)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	@GetMapping("/skill/{skill}")
	public ResponseEntity<List<Technician>> getBySkill(@PathVariable String skill) {
		List<Technician> technicians = technicianService.findBySkillSet(skill);
		if (technicians.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(technicians);
	}
	@GetMapping("/status/{status}")
	public ResponseEntity<List<Technician>> getByStatus(@PathVariable String status) {
		List<Technician> technicians = technicianService.findByStatus(status);
		if (technicians.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(technicians);
	}
	@GetMapping("/{id}")
	public ResponseEntity<Technician> getById(@PathVariable Integer id) {
		return technicianService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	@DeleteMapping("/{id}/delete")
	public ResponseEntity<Void> deleteTechnician(@PathVariable Integer id) {
		technicianService.deleteTechnician(id);
		return ResponseEntity.noContent().build();
	}
	// Additional endpoints can be added as needed
}
